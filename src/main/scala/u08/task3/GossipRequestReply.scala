package scala.u08.task3

import java.util.Random
import u08.modelling.{CTMCSimulation, DAP, DAPGrid}
import u08.modelling.CTMCSimulation.*
import scala.u08.utils.{Grids, MSet}

object GossipRequestReply:
  enum Place:
    case REQUEST, REPLY, IDLE
  type ID = (Int, Int)
  export Place.*
  export u08.modelling.DAP.*
  export u08.modelling.DAPGrid.*
  export u08.modelling.CTMCSimulation.*

  val requestReplyRules = DAP[Place](
    Rule(MSet(REQUEST), m => 1.0, MSet(REPLY), MSet()), // REQUEST -> REPLY
    Rule(MSet(REPLY), m => 1.0, MSet(IDLE), MSet())     // REPLY -> IDLE
  )

  val requestReplyCTMC = DAP.toCTMC[ID, Place](requestReplyRules)
  val net = Grids.createRectangularGrid(5, 5)
  val initialState = State[ID, Place](MSet(Token((0, 0), REQUEST)), MSet(), net)

@main def simulateRequestReply =
  import GossipRequestReply.*
  val trace = requestReplyCTMC.newSimulationTrace(initialState, new Random).take(50).toList
  trace.foreach { step =>
    println(s"Time: ${step._1},\nState: ${step._2}")
    println
  }