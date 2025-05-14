package scala.u06.task5.write

import u06.utils.MSet
import u06.modelling.PetriNet
import u06.modelling.SystemAnalysis

object PNProducerConsumer:
  // Define the control places of the Petri Net
  enum Place:
    case Empty, Full

  // Bring the enum values and DSL into scope
  export Place.*
  export u06.modelling.PetriNet.*
  export u06.modelling.SystemAnalysis.*
  export u06.utils.MSet

  // DSL-like specification of a simple producer-consumer Petri Net
  def pnProducerConsumer = PetriNet[Place](
    // Producer: when buffer is Empty, produce a token (add Full)
    MSet(Empty) ~~> MSet(Full),
    // Consumer: when buffer is Full, consume a token (remove Full, return to Empty)
    MSet(Full)  ~~> MSet(Empty)
  ).toSystem

//  @main def mainPNProducerConsumer() =
//    import PNProducerConsumer.*
//    // Starting marking: buffer empty
//    val initial = MSet(Empty)
//    // Compute all reachable markings up to 5 steps
//    val reachable = pnProducerConsumer.reachableFrom(initial, 5)
//    println("Reachable markings in up to 5 steps:")
//    reachable.toList.foreach(m => println(m.toList))
