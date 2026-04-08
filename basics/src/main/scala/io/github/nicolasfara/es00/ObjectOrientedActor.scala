package io.github.nicolasfara.es00

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import io.github.nicolasfara.es00.Counter.Command


class Counter(context: ActorContext[Command], private var from: Int, val to: Int) extends AbstractBehavior[Command](context):
	def onMessage(msg: Command): Behavior[Command] = msg match
		case Command.Tick if from < to =>
			context.log.info(s"Counter: $from")
			from += 1
			this
		case _ => Behaviors.stopped		
	

object Counter:
	enum Command:
		case Tick

	export Command.*

	def apply(to: Int): Behavior[Command] = Behaviors.setup(ctx => new Counter(ctx, 0, to))

@main def runObjectOrientedActor(): Unit =
	val system = ActorSystem(Counter(10), "HelloAkka")
	for _ <- 0 until 20 do
		system ! Command.Tick