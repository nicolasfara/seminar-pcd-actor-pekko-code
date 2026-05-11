package io.github.nicolasfara.es00

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

import scala.concurrent.duration.DurationInt

object TimerActor:
  enum Command:
    case StartTimer
    case Tick

  export Command.*

  // def apply(): Behavior[Command] = Behaviors.setup: ctx =>
  //   Behaviors.withTimers: timers =>
  //     timers.startTimerAtFixedRate(Command.Tick, 1.second)
  //     Behaviors.receiveMessagePartial:
  //       case Tick =>
  //         ctx.log.info("Timer ticked")
  //         Behaviors.same

  def apply(): Behavior[Command] = Behaviors.setup: ctx =>
    idle(ctx)

  def idle(ctx: ActorContext[Command]): Behavior[Command] = Behaviors.receiveMessagePartial:
    case Command.StartTimer =>
      Behaviors.withTimers: timers =>
        timers.startSingleTimer(Tick, 2.seconds)
        active(ctx)

  def active(ctx: ActorContext[Command]): Behavior[Command] = Behaviors.receiveMessagePartial:
    case Command.Tick =>
      ctx.log.info("Timer Ticked (once)")
      idle(ctx)

  @main def runTimerActor(): Unit =
    val system = ActorSystem(TimerActor(), "TimerActorSystem")
    system ! Command.StartTimer
    Thread.sleep(2500)
    system ! Command.StartTimer
    system ! Command.StartTimer
    system ! Command.StartTimer


