package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingBehaviorExercises.Counter.{Decrement, Increment, Print}

object ChangingBehaviorExercises extends App {

  val system = ActorSystem("main")

  /** Exercises 1
   *  recreate the counter Actor with context.become and no mutable state
   */

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {

    override def receive: Receive = receiveCount(0)

    def receiveCount(n: Int): Receive = {
      case Increment => context.become(receiveCount(n+1))
      case Decrement => context.become(receiveCount(n-1))
      case Print => println(s"$self current count = $n")
    }
  }

  val counter = system.actorOf(Props[Counter], "firstCounter")

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print


  /** Exercise 2: simplified voting system
   *
   *
   */

  type Candidate = Option[String]
  case class Vote(candidate: Candidate)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Candidate)

  class Citizen extends Actor {

    override def receive: Receive = receiveVote(None)

    def receiveVote(candidate: Candidate): Receive = {
      case Vote(candidate) => context.become(receiveVote(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {

    override def receive: Receive = receiveAggregate(Set(), Map())

    def receiveAggregate(stillWaiting: Set[ActorRef],result: Map[String, Int]): Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(c => c ! VoteStatusRequest)
        context.become(receiveAggregate(citizens, result))

      case VoteStatusReply(None) =>
        sender() ! VoteStatusRequest

      case VoteStatusReply(Some(candidate)) =>
        val waiting = stillWaiting - sender()
        val votes = result.getOrElse(candidate, 0) + 1
        val newResult = result + (candidate -> votes)
        if (waiting.isEmpty)
          println(newResult)
        else
        context.become(receiveAggregate(waiting, newResult))
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote(Some("Martin"))
  bob ! Vote(Some("Jonas"))
  charlie ! Vote(Some("Roland"))
  daniel ! Vote(Some("Roland"))

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /**
   *  print the status of votes
   *  EX:
   *  Martin -> 1
   *  Jonas  -> 1
   *  Roland -> 2
   */

}
