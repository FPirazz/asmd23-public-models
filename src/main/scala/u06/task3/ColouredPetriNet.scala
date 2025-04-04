package scala.u06.task3

import u06.utils.MSet
import u06.modelling.System.*, u06.modelling.System

object ColouredPetriNet:
  // pre-conditions, effects, inhibition
  case class ColouredTrn[P, C](cond: ColouredMarking[P, C], eff: ColouredMarking[P, C], inh: ColouredMarking[P, C])
  type ColouredPetriNet[P, C] = Set[ColouredTrn[P, C]]
  type ColouredMarking[P, C] = MSet[ColouredToken[P, C]]

  // factory of A Coloured Petri Net
  def apply[P, C](transitions: ColouredTrn[P, C]*): ColouredPetriNet[P, C] = transitions.toSet

  // factory of a System, as a toSystem method
  extension [P, C](pn: ColouredPetriNet[P, C])
    def toSystem: System[ColouredMarking[P, C]] = m =>
      for
        ColouredTrn(cond, eff, inh) <- pn   // get any transition
        if m disjoined inh          // check inhibition
        out <- m extract cond       // remove precondition
      yield out union eff           // add effect

  // fancy syntax to create transition rules
  extension [P, C](self: ColouredMarking[P, C])
    def ~~> (y: ColouredMarking[P, C]) = ColouredTrn(self, y, MSet())
  extension [P, C](self: ColouredTrn[P, C])
    def ^^^ (z: ColouredMarking[P, C]) = self.copy(inh = z)