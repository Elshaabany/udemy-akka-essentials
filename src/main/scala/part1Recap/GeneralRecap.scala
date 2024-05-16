package part1Recap

import scala.concurrent.Future
import scala.language.implicitConversions

object GeneralRecap extends App {
  def f: (Int => Int) = (x: Int) => x + 1

  val pf: PartialFunction[Int, Int] = {
    case 1 => 11
    case 2 => 22
  }

  val pf2 = (x: Int) => x match {
    case 1 => 42
    case 2 => 53
    case _ => 0
  }

  val liftedPf = pf.lift

  // partial functions are extensions of normal functions
  val normalFunction: Int => Int = pf

  // will throw matcher error
//  println(pf(22))
  println(liftedPf(22))
  println(pf2(1))

  // type aliases
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 404 => println("not found")
    case _   => println("server error")
  }


  implicit val timeOut: Int = 3000

  def setTimeout(f: () => Unit)(implicit t: Int) = f()

  setTimeout(() => println("time is out"))


  // implicits conversions
  // 1) implicit defs
  case class Person(name: String) {
    def greet = s"hi my name is $name"
  }
  implicit def fromStringToPerson(string: String): Person = Person(string)
  "Peter".greet
  // the compiler will do:  fromStringToPerson("Peter").greet


  // 2) implicit classes
  implicit class Dog(name: String) {
    def bark = println("bark!")
  }

  "lassie".bark
  // the compiler will do: new Dog("lassie").bark

  // organize
  // local scope

  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  val reverseList = List(1,2,3).sorted
  println(reverseList)


  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("hello from future")
  }

  // companion objects of the types included in the call
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  val sortedPersons = List(Person("Bob"), Person("Ali")).sorted
  println(sortedPersons)


}
