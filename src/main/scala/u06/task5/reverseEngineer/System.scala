// Package for modeling systems as Petri nets
package scala.u06.task5.reverseEngineer

// Core System type
case class System[T](
                      transitions: Set[(T, T)]
                    ) {
  // Return direct successors of a given state
  def next(from: T): Set[T] =
    transitions.collect { case (s, t) if s == from => t }

  // Normalize a state by walking until a terminal state (no outgoing transitions)
  def normalForm(start: T): T = {
    @annotation.tailrec
    def loop(current: T): T =
      next(current).headOption match {
        case Some(nextState) => loop(nextState)
        case None            => current
      }
    loop(start)
  }

  // All paths of exactly 'depth' transitions starting from 'start'
  def paths(start: T, depth: Int): Iterable[List[T]] = {
    def loop(current: T, steps: Int): Iterable[List[T]] =
      if (steps == 0) Iterable(List(current))
      else
        next(current).flatMap { succ =>
          loop(succ, steps - 1).map(current :: _)
        }
    loop(start, depth)
  }

  // All paths up to a maximum depth
  def completePathsUpToDepth(start: T, maxDepth: Int): Iterable[List[T]] =
    (0 to maxDepth).flatMap(paths(start, _))
}

object System {
  // Construct a System from a variable number of transitions
  def ofTransitions[T](trs: (T, T)*): System[T] =
    System(trs.toSet)
}

// Analysis utilities
object SystemAnalysis {
  extension[T] (sys: System[T]) {
    def next(from: T): Set[T]       = sys.next(from)
    def normalForm(start: T): T     = sys.normalForm(start)
    def paths(start: T, d: Int): Iterable[List[T]] = sys.paths(start, d)
    def completePathsUpToDepth(start: T, d: Int): Iterable[List[T]] = sys.completePathsUpToDepth(start, d)
  }
}
