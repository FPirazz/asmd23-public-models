package scala.u07.task5.improve

import scala.u07.task5.completeImplement.WeightedSampler

object Demo extends App {
  import scala.util.Random

  // Say you have three outcomes with weights 0.1, 0.3, 0.6:
  val outcomes = Seq("apple" -> 0.1, "banana" -> 0.3, "cherry" -> 0.6)

  // Create a sampler (you can pass in your own Random for reproducibility)
  val sampler = WeightedSampler(outcomes, new Random(12345L))

  // Draw 10 samples:
  val draws = (1 to 10).map(_ => sampler.sample())
  println(draws.mkString(", "))
}
