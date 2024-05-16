package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {

    override def receive: Receive = {
      case "Hi!" => sender() ! "Hello, there!"
      case message: String => println(s"[$self] I have received: $message")
      case number: Int => println(s"[$self] I have received a number: $number")
      case SpecialMessage(contents) => self ! s"[Special Message]: $contents"
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward content
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  case class SpecialMessage(contents: String)
  simpleActor ! "hello, actor"
  simpleActor ! 3
  simpleActor ! SpecialMessage("this is a special message")

  // actor can send messages to the sender

  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)
  alice ! "Hi!"

  // actors can forward messages

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi!", bob)
}
