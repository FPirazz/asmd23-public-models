package scala.u06.task3

import u06.utils.MSet
import u06.modelling.System.*, u06.modelling.System

object PetriNetPriority:
  // pre-conditions, effects, inhibition
  case class Trn[P](cond: MSet[P], eff: MSet[P], inh: MSet[P], p: Int)
  type PetriNet[P] = Set[Trn[P]]
  type Marking[P] = MSet[P]

  // factory of A Petri Net
  def apply[P](transitions: Trn[P]*): PetriNet[P] = transitions.toSet

  // factory of a System, as a toSystem method
  extension [P](pn: PetriNet[P])
    def toSystem: System[Marking[P]] = m =>
      val transitionsSorted = pn.toSeq.sortBy(-_.p)
      (for
        Trn(cond, eff, inh, _) <- transitionsSorted   // get any transition
        if m disjoined inh          // check inh  ibition
        out <- m extract cond       // remove precondition
      yield out union eff).toSet      // add effect and converts to Seq

  // fancy syntax to create transition rules
  extension [P](self: Marking[P])
    def ~~> (y: Marking[P]) = Trn(self, y, MSet(), 0)
  extension [P](self: Trn[P])
    def ^^^ (z: Marking[P]) = self.copy(inh = z)
  extension[P] (self: Trn[P] )
    def ~~^ (p2: Int) = self.copy(p = p2)