package playground

import akka.actor.ActorSystem

import scala.collection.mutable

object Playground extends App {
  println(Solution.isStrictlyPalindromic(9))
}



object Solution {
  def isStrictlyPalindromic(n: Int): Boolean = {
//    val binaryString = n.toBinaryString
//    val binaryStringLength = binaryString.length
//    if (binaryStringLength % 2 != 0) return false
//    val stack = mutable.Stack[Char]()
//    for (idx <- 0 until binaryStringLength / 2) {
//      stack.push(binaryString(idx))
//    }
//
//    for (idx <- binaryStringLength / 2 until binaryStringLength) {
//      if (stack.head == binaryString(idx)) stack.pop
//    }
//    stack.isEmpty



    def isPalindrom(s: java.lang.String): Boolean = {
      val length = s.length
      if (length % 2 != 0) return false
      for (idx <- 0 until length / 2) {
        if (s(idx) != s(length - idx - 1)) return false
      }
      true
    }

    var flag = true

    (2 to n - 2).foreach( x =>
      Integer.toString(n, x) match {
        case s if !isPalindrom(s) => flag = false
      }
    )

    flag
  }
}
// 1001