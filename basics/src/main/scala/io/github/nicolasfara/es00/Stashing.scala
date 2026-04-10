package io.github.nicolasfara.es00

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout

import scala.collection.concurrent.TrieMap
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration.*
import scala.util.Failure
import scala.util.Success
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import io.github.nicolasfara.es00.Stashing.Command
import io.github.nicolasfara.es00.Stashing.Command.*

trait DB:
  def store(id: UUID, data: String): Future[Unit]
  def load(id: UUID): Future[String]

class MockDB(using executionContext: ExecutionContext) extends DB:
  private val data = TrieMap.empty[UUID, String]

  def store(id: UUID, value: String): Future[Unit] = Future:
    data.update(id, value)
    ()

  def load(id: UUID): Future[String] = Future:
    data.getOrElse(id, "")

class SlowMockDB(
  initialData: Map[UUID, String],
  loadDelay: FiniteDuration,
  storeDelay: FiniteDuration
) extends DB:
  private val data = TrieMap.from(initialData)

  def store(id: UUID, value: String): Future[Unit] =
    val promise = Promise[Unit]()
    println(s"[db] storing '$value' for $id, this will take $storeDelay")
    CompletableFuture.delayedExecutor(storeDelay.toMillis, TimeUnit.MILLISECONDS).execute: () =>
      data.update(id, value)
      println(s"[db] store completed for $id")
      promise.success(())
    promise.future

  def load(id: UUID): Future[String] =
    val promise = Promise[String]()
    println(s"[db] loading state for $id, this will take $loadDelay")
    CompletableFuture.delayedExecutor(loadDelay.toMillis, TimeUnit.MILLISECONDS).execute: () =>
      val loaded = data.getOrElse(id, "")
      println(s"[db] load completed for $id with '$loaded'")
      promise.success(loaded)
    promise.future

object Stashing:
  enum Command:
    case Save(value: String, replyTo: ActorRef[Unit])
    case Get(replyTo: ActorRef[String])
    case InitialState(value: String)
    case SaveSuccess
    case DBError(cause: Throwable)

  def apply(id: UUID, db: DB): Behavior[Command] = Behaviors.withStash(100): stashBuffer =>
    Behaviors.setup: context =>
      new Stashing(context, stashBuffer, id, db).start()

class Stashing(
  context: ActorContext[Command],
  buffer: StashBuffer[Command],
  id: UUID,
  db: DB
):
  private def start(): Behavior[Command] =
    context.log.info("Starting actor {}, loading state from the DB", id)
    context.pipeToSelf(db.load(id)):
      case Success(maybeValue) => Command.InitialState(maybeValue)
      case Failure(exception)  => Command.DBError(exception)

    Behaviors.receiveMessage:
      case InitialState(value) =>
        context.log.info("Initial DB state loaded: '{}'. Replaying stashed messages.", value)
        buffer.unstashAll(active(value))
      case DBError(cause) =>
        context.log.error(s"Failed to load initial state for id $id", cause)
        throw cause
      case cmd =>
        context.log.info("Stashing {} while waiting for the initial DB state", cmd)
        val _ = buffer.stash(cmd)
        Behaviors.same

  private def active(state: String): Behavior[Command] = Behaviors.receiveMessagePartial:
    case Get(replyTo) =>
      context.log.info("Replying with current state '{}'", state)
      replyTo ! state
      Behaviors.same
    case Save(value, replyTo) =>
      context.log.info("Received Save('{}'), persisting it to the DB", value)
      context.pipeToSelf(db.store(id, value)):
        case Success(_)     => Command.SaveSuccess
        case Failure(error) => Command.DBError(error)
      saving(value, replyTo)

  private def saving(state: String, replyTo: ActorRef[Unit]): Behavior[Command] = Behaviors.receiveMessage:
    case SaveSuccess =>
      context.log.info("Save completed. Replaying messages stashed during the DB write.")
      replyTo ! ()
      buffer.unstashAll(active(state))
    case DBError(cause) =>
      context.log.error(s"Failed to save state for id $id", cause)
      throw cause
    case cmd =>
      context.log.info("Stashing {} while a DB write is in progress", cmd)
      val _ = buffer.stash(cmd)
      Behaviors.same

@main def runStashing(): Unit =
  given Timeout = 10.seconds

  val entityId = UUID.randomUUID()
  val system = ActorSystem(
    Behaviors.setup[Command]: _ =>
      val db = new SlowMockDB(
        initialData = Map(entityId -> "state loaded from the DB"),
        loadDelay = 4.seconds,
        storeDelay = 2.seconds
      )
      Stashing(entityId, db),
    "StashingExample"
  )

  given ActorSystem[Command] = system
  given ExecutionContext = system.executionContext

  val firstRead = system ? (replyTo => Get(replyTo))
  val save = system ? (replyTo => Save("hello from stashing", replyTo))
  val secondRead = system ? (replyTo => Get(replyTo))

  firstRead.onComplete:
    case Success(value) => println(s"[client] first Get completed with: '$value'")
    case Failure(error) => println(s"[client] first Get failed: ${error.getMessage}")

  save.onComplete:
    case Success(_)     => println("[client] Save completed")
    case Failure(error) => println(s"[client] Save failed: ${error.getMessage}")

  secondRead.onComplete:
    case Success(value) => println(s"[client] second Get completed with: '$value'")
    case Failure(error) => println(s"[client] second Get failed: ${error.getMessage}")

  Await.result(firstRead, 10.seconds)
  Await.result(save, 10.seconds)
  Await.result(secondRead, 10.seconds)

  system.terminate()
