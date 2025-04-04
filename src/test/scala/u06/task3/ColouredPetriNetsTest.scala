package scala.u06.task3

import scala.u06.task3.ColouredPetriNet, scala.u06.task3.ColouredPetriNet.*
import u06.utils.MSet, u06.utils.MSet.*

object ColouredPetriNetsTest:

  enum Place:
    case P1, P2, P3

  import Place.*

  // Define a Coloured Petri Net
  val colouredPetriNet = ColouredPetriNet[Place, String](
    MSet(ColouredToken(P1, "red")) ~~> MSet(ColouredToken(P2, "blue")),
    MSet(ColouredToken(P1, "green"), ColouredToken(P2, "blue")) ~~> MSet(ColouredToken(P3, "yellow"))
  )

  val initialState = MSet(ColouredToken(P1, "red"))
  val system = colouredPetriNet.toSystem

  @main def testColouredPetriNet() =
    val transState = system.next(initialState)
    println(s"First state:  $initialState")
    println(s"Transitioned state: $transState")