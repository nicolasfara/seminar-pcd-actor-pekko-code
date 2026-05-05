package io.github.nicolasfara.es01.cluster.joining

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.cluster.typed.Cluster
import org.apache.pekko.cluster.typed.Join
import org.apache.pekko.cluster.typed.Leave
import com.typesafe.config.ConfigFactory

@main def joining(): Unit =
  def createActorSystem[A](port: Int, behavior: Behavior[A]) =
    val config = ConfigFactory.parseString(s"""
      pekko.remote.artery.canonical.port = $port
      """).withFallback(ConfigFactory.load("application-joining.conf"))
    ActorSystem[A](behavior, "ClusterSystem", config)

  val system1 = createActorSystem(25251, Behaviors.empty)
  val system2 = createActorSystem(25252, Behaviors.empty)
  val clusterSystem1 = Cluster(system1)
  clusterSystem1.manager ! Join(clusterSystem1.selfMember.address)
  system1.log.info("Started first node")
  Thread.sleep(5000)
  val clusterSystem2 = Cluster(system2)
  clusterSystem2.manager ! Join(clusterSystem1.selfMember.address)
  Thread.sleep(5000)
  system2.log.info("Started second node")
  // ---- Read cluster state
  system1.log.info(s"First node members: ${clusterSystem1.state}")
  // Verify two cluster state are the same
  assert(clusterSystem1.state == clusterSystem2.state)
  // ---- Leave cluster
  clusterSystem1.manager ! Leave(clusterSystem2.selfMember.address)
  Thread.sleep(5000)
  system1.log.info(s"First node members after leaving: ${clusterSystem1.state}")
  system1.terminate()
  system2.terminate()
