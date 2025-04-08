package scala.u07.task1

import u07.utils.Time

import java.util.Random
import u07.examples.StochasticChannel.*

object StochasticChannelAnalysis:

  @main def mainAverageTimeToDone =
    val n = 1000 // Number of runs
    val avgTime = SimulationAnalysis.averageTimeToState(n, IDLE, DONE, stocChannel.newSimulationTrace)
    println(s"Average time to DONE across $n runs: $avgTime seconds")
  
  
  @main def mainRelativeTimeInFail =
    val n = 1000 // Number of runs
    val avgTime = SimulationAnalysis.relativeTimeInState(n, IDLE, DONE, FAIL, stocChannel.newSimulationTrace)
    println(s"Average time to DONE across $n runs: $avgTime seconds")
