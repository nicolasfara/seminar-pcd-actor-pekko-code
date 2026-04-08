package io.github.nicolasfara.es00

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*

object CounterActor:
  enum Command:
    case Tick

  export Command.*

  def apply(from: Int, to: Int): Behavior[Command] = Behaviors.receive: (context, message) =>
    message match
      case Tick if from < to =>
        context.log.info(s"Counter: $from")
        CounterActor(from + 1, to)
      case _ => Behaviors.stopped

@main def runFunctionalActor(): Unit =
  val system = ActorSystem(CounterActor(0, 10), "HelloAkka")
  for _ <- 0 until 20 do
    system ! CounterActor.Tick
