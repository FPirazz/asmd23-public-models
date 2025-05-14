
package scala.u07.task5.write

import scala.util.Random

object RandomFromDistribution {
  def sample[T](values: Seq[(T, Double)]): T = {
    val total = values.map(_._2).sum
    val r = Random.nextDouble() * total
    var cumulative = 0.0

    for ((value, weight) <- values) {
      cumulative += weight
      if (r <= cumulative) return value
    }

    values.last._1 // fallback, should not happen if weights > 0
  }

  def main(args: Array[String]): Unit = {
    val dist = Seq(("apple", 0.1), ("banana", 0.3), ("cherry", 0.6))
    val samples = for (_ <- 1 to 10) yield sample(dist)
    println(samples.mkString(", "))
  }
}
