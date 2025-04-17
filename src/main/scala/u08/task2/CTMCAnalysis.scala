package scala.u08.task2

import java.util.Random

object CTMCAnalysis:

  import u08.modelling.CTMCSimulation.*
  import u08.modelling.CTMC

  extension [S](self: CTMC[S])
    // G formula: Check if a property holds globally within a time bound
    def globally(runs: Int, prop: S => Boolean, s0: S, timeBound: Double): Double =
      (0 until runs).count: _ =>
        self.newSimulationTrace(s0, new Random)
          .takeWhile(_.time <= timeBound)
          .forall(e => prop(e.state))
      .toDouble / runs

    // Steady-state computation: Approximate the proportion of time spent in a given state
    def steadyState(runs: Int, s0: S, targetState: S, totalTime: Double): Double =
      (0 until runs).map: _ =>
        val trace = self.newSimulationTrace(s0, new Random)
        val events = trace.takeWhile(_.time <= totalTime).toList
        val totalDuration = events.lastOption.map(_.time).getOrElse(0.0)
        val targetDuration = events.sliding(2).collect:
          case List(Event(t1, s1), Event(t2, s2)) if s1 == targetState => t2 - t1
        .sum
        targetDuration / totalDuration
      .sum / runs