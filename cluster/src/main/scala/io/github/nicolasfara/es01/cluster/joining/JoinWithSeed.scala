package io.github.nicolasfara.es01.cluster.joining

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.cluster.typed.*
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.AddressFromURIString

val seeds = Seq(2551, 2552)

def createActorSystem[A](port: Int, behavior: Behavior[A]) =
  val config = ConfigFactory.parseString(s"""
    pekko.remote.artery.canonical.port = $port
    """).withFallback(ConfigFactory.load("application-seed.conf"))

  ActorSystem[A](behavior, "ClusterSystem", config)

@main def spawnSeeds(): Unit =
  seeds.foreach { port =>
    createActorSystem(port, Behaviors.empty)
  }

@main def joinWithSeed(myPort: Int, seedPort: Int): Unit =
  val system = createActorSystem(myPort, Behaviors.empty)
  val seed = AddressFromURIString(s"pekko://ClusterSystem@127.0.0.1:$seedPort")
  Cluster(system).manager ! Join(seed)
