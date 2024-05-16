package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Kid.{KidAccept, KidReject}
import part2actors.ChangingActorBehavior.Mom.{Ask, CHOCOLATE, Food, MomStart, VEGETABLE}

object ChangingActorBehavior extends App {

  /**
   * we want to make stateless actors to:
   * - achieve immutability
   * - reduce complex logic based on the state
   */

  // context.become

  class Kid extends Actor {
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, discardOld = true)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.become(happyReceive)
      case Ask(_) => sender() ! KidReject
    }
  }

  object Kid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }

  class Mom extends Actor {

    override def receive: Receive = {
      case MomStart(kid) =>
        kid ! Food(VEGETABLE)
        kid ! Ask("Do you love me?")
      case KidAccept => println("my kid is happy")
      case KidReject => println("I'm doing this for your benefit")
    }
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "vegetable"
    val CHOCOLATE = "chocolate"
  }

  val system = ActorSystem("main")
  val kiddo = system.actorOf(Props[Kid], "myKid")
  val mom = system.actorOf(Props[Mom], "theMother")

  mom ! MomStart(kiddo)

}
