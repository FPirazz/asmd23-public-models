package scala.u07.task2

import u07.modelling.SPN
import u07.modelling.SPN.Trn
import u07.utils.MSet

import java.util.Random

object StochasticReadersWriters:

  enum Place:
    case ProcessIdle, ProcessReady, ReaderReady, WriterReady, ReaderRunning, WriterRunning

  export Place.*

  // DSL-like specification of a Petri Net that models a Stochastic Readers/Writers as per Slides
  def spn = SPN[Place](
    Trn(MSet(ProcessIdle), m => 1.0, MSet(ProcessReady), MSet()),

    Trn(MSet(ProcessReady), m => 200000.0, MSet(ReaderReady), MSet()),
    Trn(MSet(ProcessReady), m => 100000.0, MSet(WriterReady), MSet()),

    Trn(MSet(ReaderReady), m => 100000.0, MSet(ReaderRunning), MSet(WriterRunning)),
    Trn(MSet(ReaderRunning), m => 0.1, MSet(ProcessIdle), MSet()),


    Trn(MSet(WriterReady), m => 100000.0, MSet(WriterRunning), MSet(WriterRunning, ReaderRunning)),
    Trn(MSet(WriterRunning), m => 0.2, MSet(ProcessIdle), MSet()),
  )

  @main def mainPNReadersWriters =
    import StochasticReadersWriters.*
    import u07.modelling.CTMCSimulation.*

    val initialMarking = MSet(ProcessIdle, ProcessIdle)
    val CTMCmodel = SPN.toCTMC(spn)

    println:
      CTMCmodel.newSimulationTrace(initialMarking, new Random)
        .take(20)
        .toList
        .mkString("\n")

