package scala.u07.task1

import u07.utils.Time

import java.util.Random
import u07.examples.StochasticChannel.*

object StochasticChannelAnalysis:

  def averageTimeToDone(n: Int): Double =
    val rnd = new Random
    val times = (1 to n).flatMap { _ =>
      stocChannel.newSimulationTrace(IDLE, rnd)
        .find(_.state == DONE) // Find the first event where state is DONE
        .map(_.time)           // Extract the time of that event
    }
    if times.nonEmpty then times.sum / times.size else Double.NaN

  def relativeTimeInFail(n: Int): Double =
    val rnd = new Random
    val failRatios = (1 to n).map { _ =>

      val tmp = stocChannel.newSimulationTrace(IDLE, rnd)
        .map(s => (s.state, s.time))
      val result = tmp.takeWhile((s, t) => s != DONE) :+ tmp.find((s, t) => s == DONE).get
      val totalTime = result.find((s, t) => s == DONE).map((s, t) => t).get
      val failTime = result.filter((s, t) => s == FAIL).map((s, t) => t).sum
    
      failTime / totalTime
    }
    if failRatios.nonEmpty then failRatios.sum / failRatios.size else Double.NaN

@main def mainAverageTimeToDone =
  Time.timed:
    val n = 1// Number of runs
    val avgTime = StochasticChannelAnalysis.averageTimeToDone(n)
    println(s"Average time to DONE across $n runs: $avgTime seconds")


@main def mainRelativeTimeInFail =
  Time.timed:
    val n = 100 // Number of runs
    val avgTime = StochasticChannelAnalysis.relativeTimeInFail(n)
    println(s"Average time to DONE across $n runs: $avgTime seconds")
