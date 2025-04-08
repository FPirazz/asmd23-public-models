package scala.u07.task1

import u07.modelling.CTMCSimulation.Event
import scala.collection.immutable.LazyList
import java.util.Random

object SimulationAnalysis:

  /** Helper method to generate traces and apply a function to each run. */
  private def runSimulations[S, T](n: Int, initialState: S, traceGenerator: (S, Random) => LazyList[Event[S]])(processTrace: LazyList[Event[S]] => Option[T]): Seq[T] =
    val rnd = new Random
    (1 to n).flatMap { _ =>
      val trace = traceGenerator(initialState, rnd)
      processTrace(trace)
    }

  /** Computes the average time to reach a specific state across multiple runs. */
  def averageTimeToState[S](n: Int, initialState: S, endState: S, traceGenerator: (S, Random) => LazyList[Event[S]]): Double =
    val times = runSimulations(n, initialState, traceGenerator) { trace =>
      trace.find(_.state == endState).map(_.time) // Find the first event where the state matches the target
    }
    if times.nonEmpty then times.sum / times.size else Double.NaN

  /** Computes the relative time spent in a specific state across multiple runs. */
  def relativeTimeInState[S](n: Int, initialState: S, endState: S, targetState: S, traceGenerator: (S, Random) => LazyList[Event[S]]): Double =
    val ratios = runSimulations(n, initialState, traceGenerator) { trace =>
      val events = trace.map(event => (event.state, event.time))
      val result = events.takeWhile((state, _) => state != endState) :+ events.find((state, _) => state == endState).getOrElse((endState, 0.0))
      val targetTime = result.filter((state, _) => state == targetState).map((_, time) => time).sum
      val totalTime = result.find((state, _) => state == endState).map((_, time) => time).getOrElse(0.0)
      if totalTime > 0 then Some(targetTime / totalTime) else None
    }
    
    if ratios.nonEmpty then ratios.sum / ratios.size else Double.NaN