package part1Recap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap  extends App {

  // creating threads on the JVM
  val aThread = new Thread(() => println("running on separate thread"))
  // run the thread
  aThread.start()

  // wait for the thread to finish
  aThread.join()

  // the problem with threads:
  // - unpredictable

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("Hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  threadHello.start()
//  threadHello.join()
  threadGoodbye.start()

  println("after threads start")

  class BankAccount(private var amount: Int) {
    override def toString: String = amount.toString

    // -= is not Atomic
    // two threads can read the same amount value at the same time
    def withdraw(money: Int): Unit = this.amount -= money

    // synchronized will block other threads until current thread finishes
    def safeWithdraw(money: Int): Unit = this.synchronized {
      this.amount -= money
    }

  }

  // inter-thread communication
  // wait and notify mechanism

  // Futures
  val meaningOfLife = Future {
    // futures execute in different threads
    // long computation
    42
  }

  meaningOfLife.onComplete {
    case Success(i) => println("meaning of life is " + i)
    case Failure(_) => println("404 meaning of life not found")
  }


  val aProcessedFuture = meaningOfLife.map(_ + 1)
  val aFlatProcessedFuture = meaningOfLife.flatMap { value =>
    Future(value + 1)
  }
  val filterdFuture = meaningOfLife.filter(_ % 2 == 0)
  // if the value doesn't pass the filter it will fail with no such element exception

  // for comprehensions
  val aCombinedFuture = for {
    meaning <- meaningOfLife
    filtered <- filterdFuture
  } yield meaning + filtered


  //  Thread.sleep(1000)
}
