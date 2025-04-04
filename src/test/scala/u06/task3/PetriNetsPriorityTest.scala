package scala.u06.task3

import scala.u06.task3.PetriNetPriority, scala.u06.task3.PetriNetPriority.*
import u06.utils.MSet, u06.utils.MSet.*

object PetriNetsPriorityTest:

  enum Place:
    case P1, P2, P3

  import Place.*

  // We define a PN with priority
  val petriNetP = PetriNetPriority[Place](
    MSet(P1) ~~> MSet(P2) ~~^ 1,
    MSet(P1, P2) ~~> MSet(P3) ~~^ 2
  )

  val initialState = MSet(P1)
  val system = petriNetP.toSystem

  @main def testPetriNetWithPriorities() =
    val transState = system.next(initialState)
    println(s"First state:  $initialState")
    println(s"Transitioned state: $transState")
