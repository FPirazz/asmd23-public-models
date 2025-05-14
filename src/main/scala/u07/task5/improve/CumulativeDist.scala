package scala.u07.task5.improve

import scala.util.Random

class CumulativeDist[T](values: Seq[(T, Double)]) {
  // Validate
  private val positives = values.filter(_._2 > 0)
  require(positives.nonEmpty, "At least one weight must be positive")

  // Normalize weights to sum = 1
  private val normalized = {
    val total = positives.map(_._2).sum
    positives.map { case (v, w) => (v, w / total) }
  }

  // Build cumulative array
  private val (elements, cumulative) = {
    val cum = normalized
      .scanLeft(("", 0.0)) { case ((_, acc), (v, w)) => (v.toString, acc + w) }
      .tail.map(_._2)
    (normalized.map(_._1), cum)
  }

  /** Sample in O(log n) time using binary search */
  def sample(): T = {
    val r = Random.nextDouble()
    val idx = java.util.Arrays.binarySearch(cumulative.toArray, r) match {
      case i if i >= 0 => i
      case i           => -i - 1
    }
    elements(idx)
  }
}

object RandomFromDistribution {
  def main(args: Array[String]): Unit = {
    val dist = new CumulativeDist(Seq("apple" -> 0.1, "banana" -> 0.3, "cherry" -> 0.6))
    val samples = Seq.fill(10)(dist.sample())
    println(samples.mkString(", "))
  }
}
