package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.Exercises.BankAccount.{Deposit, FailedTransaction, InsufficientFundsException, Statement, SuccessfulTransaction, Withdraw, WrongAmountException}
import part2actors.Exercises.CounterActor.{Decrement, Increment, Print}
import part2actors.Exercises.Person.LiveTheLife

object Exercises extends App {

  class CounterActor extends Actor {

    var totalCount: Int = 0
    override def receive: Receive = {
      case Increment(count) => totalCount += count
      case Decrement(count) => totalCount -= count
      case Print => println(s"[$self] current count = $totalCount")
    }
  }

  object CounterActor {
    case class Increment(count: Int)
    case class Decrement(count: Int)
    case object Print
  }

  val actorSystem = ActorSystem("Main")
  val counter = actorSystem.actorOf(Props[CounterActor], "firstCounter")

  (1 to 1000).foreach(i => counter ! Increment(i))
  (1 to 1000).foreach(i => counter ! Decrement(i))
  counter ! Print


  class BankAccount extends Actor {

    var funds: Double = 0.0
    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0) sender() ! FailedTransaction(new WrongAmountException)
        else {
          funds += amount
          sender() ! SuccessfulTransaction(s"successfully deposited $amount")
        }

      case Withdraw(amount) =>
        if (amount < 0) sender() ! FailedTransaction(new WrongAmountException)
        else if (amount > funds) sender() ! FailedTransaction(new InsufficientFundsException)
        else {
          funds -= amount
          sender() ! SuccessfulTransaction(s"successfully withdraw $amount")
        }

      case Statement => sender() ! s"your balance is $funds"

    }
  }

  object BankAccount {
    case class Deposit(amount: Double)
    case class Withdraw(amount: Double)
    case class Statement()
    case class WrongAmountException() extends RuntimeException("Wrong Amount")
    case class InsufficientFundsException() extends RuntimeException("Insufficient Funds")
    case class SuccessfulTransaction(message: String)
    case class FailedTransaction(exception: Exception)
  }


  class Person extends Actor {

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Deposit(-10000)
        account ! Withdraw(9000000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  val account = actorSystem.actorOf(Props[BankAccount], "bankAccount")
  val person = actorSystem.actorOf(Props[Person], "poor")

  person ! LiveTheLife(account)

}
