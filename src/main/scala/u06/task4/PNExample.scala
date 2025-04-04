package scala.u06.task4

import u06.modelling.PetriNet
import u06.utils.MSet
import u06.modelling.SystemAnalysis.*

object PNExample:

  enum Place:
    case A, B, C

  import Place.*
  import PetriNet.*
  import u06.modelling.SystemAnalysis.*

  def examplePN = PetriNet[Place](
    MSet(A) ~~> MSet(B),
    MSet(B) ~~> MSet(C) ^^^ MSet(C),
    MSet(C) ~~> MSet()
  )

@main def mainPNExample =
  import PNExample.*
  import PNExample.Place.*


  val pn = examplePN

  // Using cached system
  val cachedSystem = pn.toCachedSystem
  println(cachedSystem.paths(MSet(A), 3).toList.mkString("\n"))

  // Using lazy system
  val lazySystem = pn.toLazySystem
  println(lazySystem.paths(MSet(A), 3).toList.mkString("\n"))