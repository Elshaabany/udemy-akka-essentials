package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.CreditCard.{AttachToAccount, CheckStatus}
import part2actors.ChildActors.NaiveBankAccount.{Deposit, InitializeAccount, Withdraw}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  // we can create Actors using other Actors

  // context.actorOf()

  class Parent extends Actor {

    override def receive: Receive = {
      case CreateChild(child) =>
        println(s"[$self] creating child")
        val childRef = context.actorOf(Props[Child], child)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(msg) => childRef forward msg
    }

  }

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Child extends Actor {

    override def receive: Receive = {
      case msg => println(s"[${self}] received : $msg from [${sender()}]")
    }
  }

  val system = ActorSystem("System")
  val parent = system.actorOf(Props[Parent], "parent")

//  parent ! CreateChild("myChild")
//  parent ! TellChild("Hello my child Child")

  /** Guardian actors:
   *    - /system = system guardian
   *    - /user   = user-level guardian
   *    - /       = the root guardian
   */
//
//
//  // select actor by path
//
//  val childSelection = system.actorSelection("/user/parent/myChild")
//  childSelection ! "I found you"
//
//  // will be delivered to dead letters
//  val nonExistingActor = system.actorSelection("/user/parent/child")
//  nonExistingActor ! "I found you!"
//

  /**
   * Danger!
   *
   * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS` REFERENCE, TO CHILD ACTORS.
   *
   * NEVER IN YOUR LIFE.
   */

  class NaiveBankAccount extends Actor {

    var balance = 0

    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card1")
        creditCardRef ! AttachToAccount(this) // shouldn't we send ActorRef!!
      case Deposit(amount) => deposit(amount)
      case Withdraw(amount) => withdraw(amount)
    }

    def deposit(amount: Int): Unit = {
      println(s"[$self] depositing $amount on top of $balance")
      balance += amount
    }

    def withdraw(amount: Int): Unit = {
      println(s"[$self] withdrawing $amount form $balance")
      balance -= amount
    }
  }

  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object InitializeAccount
  }

  class CreditCard extends Actor {

    override def receive: Receive = {
      case AttachToAccount(account) => context.become(withAccount(account))
    }

    def withAccount(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s"[$self] your message has been processed")
        println("verifying  your card ...")
        account.withdraw(1)  // shouldn't be able to change the state of other actor
    }
  }

  object CreditCard {
    case class AttachToAccount(bankAccount: NaiveBankAccount) // shouldn't we use ActorRef!!
    case object CheckStatus
  }

  val myBankAccount = system.actorOf(Props[NaiveBankAccount], "theBank")
  myBankAccount ! InitializeAccount
  myBankAccount ! Deposit(100)

  val ccSelection = system.actorSelection("/user/theBank/card1")
  ccSelection ! CheckStatus

}
