package io.github.nicolasfara.es00

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

object StartStopActor:
  def apply(): Behavior[String] = Behaviors.setup: context =>
    context.log.info(s"Actor ${context.self} started")
    val _ = context.spawn(ChildActor(), "child-actor")

    Behaviors
      .receiveMessage[String]:
        case "stop" => Behaviors.stopped
        case _      => Behaviors.same
      .receiveSignal:
        case (ctx, PostStop) =>
          context.log.info(s"Actor ${ctx.self} stopping")
          Behaviors.same

object ChildActor:
  def apply(): Behavior[String] =
    Behaviors.setup: context =>
      context.log.info(s"Actor ${context.self} started")

      Behaviors
        .receiveMessage[String](_ => Behaviors.unhandled)
        .receiveSignal:
          case (ctx, PostStop) =>
            context.log.info(s"Actor ${ctx.self} stopping")
            Behaviors.same

@main def runLifecycle(): Unit =
  val system = ActorSystem(StartStopActor(), "HelloAkka")
  system ! "stop"
