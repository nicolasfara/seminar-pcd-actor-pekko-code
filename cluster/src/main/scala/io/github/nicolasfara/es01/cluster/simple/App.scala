package io.github.nicolasfara.es01.cluster.simple

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import com.typesafe.config.ConfigFactory

object App:
  object RootBehavior:
    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
      val _ = context.spawn(ClusterListener(), "cluster-listener")
      Behaviors.empty
    }

  def main(args: Array[String]): Unit =
    val ports =
      if args.nonEmpty then args.toSeq.map(_.toInt)
      else sys.env.get("CLUSTER_PORT").flatMap(_.toIntOption).map(Seq(_)).getOrElse(Seq(25251, 25252, 0))

    ports.foreach(startup)

  private def startup(port: Int): Unit =
    val config = ConfigFactory.parseString(s"""
      pekko.remote.artery.canonical.port = $port
      """).withFallback(ConfigFactory.load())

    val _ = ActorSystem[Nothing](RootBehavior(), "ClusterSystem", config)
