package playground

import akka.actor.ActorSystem

object Playground extends App {
  val actorSystem = ActorSystem("MyActorSystem")
  println(actorSystem.name)
}
