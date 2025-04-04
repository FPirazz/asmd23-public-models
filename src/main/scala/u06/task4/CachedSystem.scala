package scala.u06.task4

import scala.collection.mutable
import u06.modelling.System

class CachedSystem[S](system: System[S]) extends System[S] {
  private val cache = mutable.Map[S, Set[S]]()

  override def next(a: S): Set[S] = {
    cache.getOrElseUpdate(a, system.next(a))
  }
}