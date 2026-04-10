package io.github.nicolasfara.es00

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

class PrintMyRefActor(context: ActorContext[String]) extends AbstractBehavior[String](context):
  def onMessage(msg: String): Behavior[String] = msg match
    case "printit" =>
      val secondRef = context.spawn(Behaviors.empty[String], "second-actor")
      println(s"Second: $secondRef")
      this
  
object PrintMyRefActor:
  def apply(): Behavior[String] = Behaviors.setup(ctx => new PrintMyRefActor(ctx))

class MainActor(context: ActorContext[String]) extends AbstractBehavior[String](context): 
  def onMessage(msg: String): Behavior[String] = msg match
    case "start" =>
      val firstRef = context.spawn(PrintMyRefActor(), "first-actor")
      println(s"First: $firstRef")
      firstRef ! "printit"
      this

object MainActor:
  def apply(): Behavior[String] = Behaviors.setup(ctx => new MainActor(ctx))

@main def run(): Unit =
  val system = ActorSystem(MainActor(), "HelloAkka")
  system ! "start"
