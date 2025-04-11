package scala.u07.task4

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._
import u07.utils.Stochastics
import scala.util.Random

class StatisticalStochasticsTest extends AnyFunSuite {

  test("statistics should produce approximate frequencies within tolerance") {
    // Use a seeded random to maintain repeatability of the test.
    implicit val rnd: Random = new Random(42)

    // Define probabilities for each choice.
    val choices = Set(1.0 -> "a", 2.0 -> "b", 3.0 -> "c")

    // Run many trials to approximate the distribution.
    val trials = 10000
    val frequencyMap = Stochastics.statistics(choices, trials)

    // Expected frequencies based on the weights: 
    // Total weight = 1+2+3 = 6 => expected percentages: a: 1/6, b: 2/6, c: 3/6.
    frequencyMap("a") shouldBe (trials * 1 / 6) +- 500
    frequencyMap("b") shouldBe (trials * 2 / 6) +- 500
    frequencyMap("c") shouldBe (trials * 3 / 6) +- 500
  }
}
