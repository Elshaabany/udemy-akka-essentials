package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object TimersSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("SchedulersTimersDemo")

  val simpleActor = system.actorOf(Props[SimpleActor])

//  import akka.actor.TypedActor.dispatcher
  import system.dispatcher

//  system.scheduler.scheduleOnce(1 second) {
//    simpleActor ! "scheduled reminder"
//  }
//
//  val routine: Cancellable = system.scheduler.schedule(1 second, 2 seconds) {
//    simpleActor ! "heartbeat"
//  }
//
//  system.scheduler.scheduleOnce(5 seconds) {
//    routine.cancel()
//  }



  /**
   * Exercise: implement a self-closing actor
   *
   * - if the actor receives a message (anything), you have 1 second to send it another message
   * - if the time window expires, the actor will stop itself
   * - if you send another message, the time window is reset
   */


  class SelfClosingActor extends Actor with ActorLogging {

     val createTimeoutWindow: () => Cancellable = () => {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }

    override def receive: Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case message =>
        log.info(s"Received $message, staying alive")
        createTimeoutWindow()
    }
  }

//    val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
//    system.scheduler.scheduleOnce(250 millis) {
//      selfClosingActor ! "ping"
//    }
//
//    system.scheduler.scheduleOnce(1 seconds) {
//      system.log.info("sending pong to the self-closing actor")
//      selfClosingActor ! "pong"
//    }

  /**
   * Timer
   */


  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers {

    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second)

      case Reminder =>
        log.info("I am alive")

      case Stop =>
        log.warning("Stopping!")
//        timers.cancel(TimerKey)
        context.stop(self)
    }
  }


    case object TimerKey
    case object Start
    case object Reminder
    case object Stop


  val timerHeartbeatActor = system.actorOf(Props[TimerBasedHeartbeatActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerHeartbeatActor ! Stop
  }




}
