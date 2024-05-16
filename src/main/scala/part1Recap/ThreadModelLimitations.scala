package part1Recap

import scala.concurrent.Future

object ThreadModelLimitations extends App {

  // thing thread models fails to address

  // 1) OOP encapsulation is only valid in the single threaded model

  class BankAccount(private var amount: Int) {
    def withdraw(money: Int): Unit = this.amount -= money
    def deposit(money: Int): Unit = this.amount += money
    def getAmount: Int = this.amount
  }

  val account = new BankAccount(2000)
  for (_ <- 1 to 1000) {
    new Thread(() => account.withdraw(1)).start()
  }

  for (_ <- 1 to 1000) {
    new Thread(() => account.deposit(1)).start()
  }
  // this will result in race condition
  // it can be solved using synchronized lock
  // but it can introduce deadlocks, live locks
  println(account.getAmount)

  /** what we need is:
   *  a data structure that is
   *   - fully encapsulated
   *   - with no locks
   */


  // 2) delegating something to a thread is a PAIN

  var task: Runnable = null

  val runningThread: Thread = new Thread(() =>
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[bg] waiting for a task")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[bg] running a task")
        task.run()
        task = null
      }
    }
  )

  def delegatedToBackgroundThread(r: Runnable) = {
    if(task == null) task = r

    runningThread.synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(1000)
  delegatedToBackgroundThread(() => println(42))
  Thread.sleep(1000)
  delegatedToBackgroundThread(() => println("this will run in background"))

  /**
   *  the problem with this mechanism that it will be
   *  a big headache in more complex scenarios
   *
   * other problems:
   * - sending other signals to running thread
   * - how to Identify which thread is:
   *   running the background task
   *   send me the signal
   *   caused an exception
   */

  /**
   * so what we need is: a data structure that which
   * - can safely receive messages
   * - can identify the sender
   * - is easily identifiable
   * - can guard against exceptions
   */

  // 3) tracing and dealing with errors is in multi threaded env is a pain

  // what if we want to get sum of 1M numbers using 10 threads
  implicit val ec = scala.concurrent.ExecutionContext.global
  val sumWorkers = (0 to 9)
   .map(i => 100000 * i until 100000 * (i + 1))
   .map(range => Future {
     if (range.contains(187543)) throw new RuntimeException("I don't like 187543")
     range.sum
   })

  val sumFuture = Future.reduceLeft(sumWorkers)(_ + _)
  sumFuture.onComplete(println)
  Thread.sleep(10000)

}
