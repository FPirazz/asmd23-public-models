package scala.u08.task3

import java.util.Random
import u08.modelling.CTMCSimulation.*
import GossipRequestReply.*
import u08.modelling.DAP.*

object StatisticalExperiment:
  def estimateReplyTime(runs: Int): Double =
    val random = new Random
    val times = for _ <- 1 to runs yield
      val trace = requestReplyCTMC.newSimulationTrace(initialState, random).takeWhile {
        case Event(_, state: State[ID, Place]) =>
          !state.tokens.asList.exists(_.p == Place.IDLE)
      }.toList
      trace.lastOption.map(_.time).getOrElse(Double.PositiveInfinity)
    times.sum / times.size

@main def runExperiment =
  val avgTime = StatisticalExperiment.estimateReplyTime(1000)
  println(s"Estimated average time for reply: $avgTime")