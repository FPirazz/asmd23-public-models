package scala.u06.task5.completeImplement

import u06.utils.MSet

object PetriNet:
  // pre-conditions, effects, inhibition
  case class Trn[P](cond: MSet[P], eff: MSet[P], inh: MSet[P])
  type PetriNet[P] = Set[Trn[P]]
  type Marking[P] = MSet[P]

  /**
   * Returns true if the transition t is enabled under marking m:
   * - all tokens in cond are in m
   * - no tokens in inh are in m
   */
  def enabled[P](net: PetriNet[P], m: Marking[P], t: Trn[P]): Boolean =
    m.matches(t.cond) && t.inh.disjoined(m)

  /**
   * Returns all enabled transitions under marking m
   */
  def enabledTransitions[P](net: PetriNet[P], m: Marking[P]): Set[Trn[P]] =
    net.filter(t => enabled(net, m, t))

  /**
   * Attempts to fire transition t on marking m:
   * - if enabled, returns Some(newMarking)
   * - else None
   * Effects: newMarking = (m \ cond) \* diff cond then union eff
   */
  def fire[P](m: Marking[P], t: Trn[P]): Option[Marking[P]] =
    if enabled(net = Set.empty, m = m, t = t) then
      val removed = m.diff(t.cond)
      val added   = removed.union(t.eff)
      Some(added)
    else None

  /**
   * Fires any one enabled transition nondeterministically
   */
  def fireOne[P](net: PetriNet[P], m: Marking[P]): Option[(Trn[P], Marking[P])] =
    enabledTransitions(net, m).headOption.flatMap { t =>
      fire(m, t).map(m2 => (t, m2))
    }

  /**
   * Computes the reachable markings in at most "steps" steps
   */
  def reachable[P](net: PetriNet[P], m0: Marking[P], steps: Int): Set[Marking[P]] =
    def rec(current: Set[Marking[P]], depth: Int): Set[Marking[P]] =
      if depth == 0 then current
      else
        val next = for
          m <- current
          t <- enabledTransitions(net, m)
          m2 <- fire(m, t)
        yield m2
        current ++ rec(next, depth - 1)
    rec(Set(m0), steps)

// Example usage:
import PetriNet._

@main def testPetri(): Unit =
  // Define places as symbols
  type Place = String
  val p = MSet.ofList[Place](List("p1", "p2"))

  // Transition t1: consumes p1, produces p2
  val t1 = Trn(cond = MSet("p1"), eff = MSet("p2"), inh = MSet())
  val net: PetriNet[Place] = Set(t1)
  val initial: Marking[Place] = MSet("p1", "p1")

  println(s"Enabled: ${enabledTransitions(net, initial)}")
  fireOne(net, initial) match
    case Some((t, m2)) => println(s"Fired $t -> $m2")
    case None          => println("No transition enabled")
  println(s"Reachable in 3 steps: ${reachable(net, initial, 3)}")
