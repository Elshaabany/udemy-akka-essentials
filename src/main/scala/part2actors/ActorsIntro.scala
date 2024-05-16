package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // part 1 - actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  /**
   * Actors are:
   * - uniquely identified
   * - can be sent messages asynchronously
   * - can respond differently
   */
  // part 2 - create actors

  class WordCountActor extends Actor {

    var totalWords = 0

    override def receive: Receive = {
      case message: String => totalWords += message.split(" ").length
      case msg => println(s"I don't understand $msg")
    }
  }

  // part 3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "firstWordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "secondWordCounter")

  // part 4 - communicate
  wordCounter ! "I love Akka"
  anotherWordCounter ! "Akka is awesome"
  println(wordCounter)


  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ => println("I don't understand")
    }
  }
  object Person {
    def props(name: String): Props = Props(new Person(name))
  }

  // creating an actor for class with constructor parameters
//  val person = actorSystem.actorOf(Props(new Person("Bob")))
  val person = actorSystem.actorOf(Person.props("eslam"))
  person ! "hi"
}
