package scala.u06.task4

import u06.modelling.System
import scala.collection.mutable

class LazySystem[S](system: System[S]) extends System[S] {
  private val lazyCache = mutable.Map[S, () => Set[S]]()

  override def next(a: S): Set[S] = {
    lazyCache.getOrElseUpdate(a, () => system.next(a))()
  }
}