package scala.u07.task5.reverseEngineer

import scala.util.Random
import scala.collection.Searching._

/**
 * A sampler that draws items of type T with probability proportional
 * to the weights provided.
 *
 * @param itemsWithWeights sequence of (item, weight > 0)
 * @param rnd              a scala.util.Random instance
 */
class WeightedSampler[T] private (
                                   itemsWithWeights: IndexedSeq[(T, Double)],
                                   cumulative:  Array[Double],
                                   totalWeight: Double,
                                   rnd:         Random
                                 ) {
  /** Draw one sample in O(log n) time */
  def sample(): T = {
    // uniform in [0.0, totalWeight)
    val u = rnd.nextDouble() * totalWeight

    // binary search for the first index where cumulative(idx) > u
    val idx = cumulative.search(u) match {
      case Found(i)     => i    // exact match (rare)
      case InsertionPoint(i) => i
    }

    itemsWithWeights(idx)._1
  }
}

object WeightedSampler {
  /**
   * Factory: builds the internal cumulative array in O(n), then each
   * sample is O(log n).
   */
  def apply[T](outcomes: Seq[(T, Double)], rnd: Random): WeightedSampler[T] = {
    require(outcomes.nonEmpty, "need at least one outcome")
    // Convert to IndexedSeq for fast random access
    val iw = outcomes.toIndexedSeq
    // Compute cumulative sums:
    val cum = new Array[Double](iw.length)
    var running = 0.0
    var i = 0
    while (i < iw.length) {
      val w = iw(i)._2
      require(w >= 0.0, s"negative weight: $w at index $i")
      running += w
      cum(i) = running
      i += 1
    }
    require(running > 0.0, "total weight must be positive")
    new WeightedSampler(iw, cum, running, rnd)
  }
}
