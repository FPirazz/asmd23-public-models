//// Extended PetriNet DSL with additional analysis operations
//
//package scala.u06.task5.improve
//
//import u06.utils.MSet
//import scala.collection.immutable.{Set, Map}
//
//object PetriNet {
//  // Basic types
//  case class Trn[P](cond: MSet[P], eff: MSet[P], inh: MSet[P])
//  type PetriNet[P] = Set[Trn[P]]
//  type Marking[P] = MSet[P]
//
//  // Factory
//  def apply[P](transitions: Trn[P]*): PetriNet[P] = transitions.toSet
//
//  // Core semantics
//  extension [P](pn: PetriNet[P]) {
//    def toSystem: System[Marking[P]] = m =>
//      for {
//        Trn(cond, eff, inh) <- pn
//        if m disjoined inh
//        out                 <- m extract cond
//      } yield out union eff
//
//    def toCachedSystem: System[Marking[P]] = new CachedSystem(toSystem)
//    def toLazySystem:  System[Marking[P]] = new LazySystem(toSystem)
//  }
//
//  // DSL syntax
//  extension [P](m: Marking[P]) def ~~> (eff: Marking[P]) = Trn(m, eff, MSet.empty)
//  extension [P](t: Trn[P])     def ^^^ (inh: Marking[P]) = t.copy(inh = inh)
//
//  // ###########################################
//  // New: firing semantics and reachability
//  // ###########################################
//
//  /**
//   * One-step firing: returns all successors of a given marking
//   */
//  extension [P](pn: PetriNet[P])
//    def fireOnce(m: Marking[P]): Set[Marking[P]] =
//      toSystem(m).toSet
//
//  /**
//   * Reachability graph up to depth n
//   */
//  extension [P](pn: PetriNet[P])
//    def reachableFrom(initial: Marking[P], depth: Int): Set[Marking[P]] = {
//      def loop(current: Set[Marking[P]], visited: Set[Marking[P]], k: Int): Set[Marking[P]] =
//        if k == 0 then visited
//        else {
//          val next = current.flatMap(pn.fireOnce) -- visited
//          loop(next, visited ++ next, k - 1)
//        }
//      loop(Set(initial), Set(initial), depth)
//    }
//
//  /**
//   * Deadlock detection: has no outgoing transitions
//   */
//  extension [P](pn: PetriNet[P])
//    def isDeadlock(m: Marking[P]): Boolean = pn.fireOnce(m).isEmpty
//
//  /**
//   * All dead markings reachable within depth n
//   */
//  extension [P](pn: PetriNet[P])
//    def deadlocksFrom(initial: Marking[P], depth: Int): Set[Marking[P]] =
//      reachableFrom(initial, depth).filter(isDeadlock)
//
//  // TODO: Add invariant computation, place capacities, timed transitions, and bisimulation checks
//}
