package part3Testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
class TestProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
with ImplicitSender
with WordSpecLike
with BeforeAndAfterAll {

  import TestProbeSpec._
  override protected def afterAll(): Unit =
    TestKit.shutdownActorSystem(system)

  // we need to test master only
  // we need something to simulate the slave actor
  // we can use TestProp for that

  "A master actor" should {
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }

    "send the work to the slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workloadString = "I love Akka"
      master ! Work(workloadString)

      // we can use the same expect method on the slave created by TestProbe

      slave.expectMsg(SlaveWork(workloadString, testActor))

      // make slave send reply to another actor
      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3)) // testActor receives the Report(3)
    }

    "aggregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workloadString = "I love Akka"
      val work2String = "Scala Rocks"
      master ! Work(workloadString)
      master ! Work(work2String)

      // in the meantime I don't have a slave actor
      slave.receiveWhile() {
        case SlaveWork(`workloadString`, `testActor`) => slave.reply(WorkCompleted(3, testActor))
        case SlaveWork(`work2String`, `testActor`) => slave.reply(WorkCompleted(2, testActor))
      }

      expectMsg(Report(3))
      expectMsg(Report(5))
    }

  }

}

object TestProbeSpec {
  /*
  word counting actor hierarchy master-slave

  send some work to the master
    - master sends the slave the piece of work
    - slave processes the work and replies to master
    - master aggregates the result
  master sends the total count to the original requester
 */

  case class Work(text: String)
  case class SlaveWork(text: String, originalRequester: ActorRef)
  case class WorkCompleted(count: Int, originalRequester: ActorRef)
  case class Register(slaveRef: ActorRef)
  case object RegistrationAck
  case class Report(totalCount: Int)

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0))
      case _ => // ignore
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalWordCount = totalWordCount + count
        originalRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }
  }
}
