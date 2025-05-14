package scala.u07.task5.completeImplement

import scala.annotation.tailrec
import scala.util.Random

class WeightedSampler[T] private (
                                   values: Vector[T],
                                   prefixSums: Vector[Double],
                                   totalWeight: Double,
                                   rng: Random
                                 ) {

  /** Draw one sample in O(log n) time via binary search on prefix sums. */
  def sample(): T = {
    // random number in [0, totalWeight)
    val r = rng.nextDouble() * totalWeight

    // binary search for the first prefixSum ≥ r
    val idx = {
      @tailrec
      def bs(lo: Int, hi: Int): Int = {
        if (lo >= hi) lo
        else {
          val mid = lo + (hi - lo) / 2
          if (prefixSums(mid) >= r) bs(lo, mid)
          else bs(mid + 1, hi)
        }
      }
      bs(0, prefixSums.length - 1)
    }

    values(idx)
  }
}

object WeightedSampler {
  /**
   * Factory method. 
   * @param seq  Sequence of (value, weight).
   * @param rng  Optional RNG; defaults to scala.util.Random.
   * @throws IllegalArgumentException if any weight is negative or all weights are zero.
   */
  def apply[T](seq: Seq[(T, Double)], rng: Random = new Random): WeightedSampler[T] = {
    require(seq.nonEmpty, "Must supply at least one (value, weight).")
    val (vs, ws) = seq.unzip
    if (ws.exists(_ < 0.0))
      throw new IllegalArgumentException("Negative weights are not allowed.")
    val total = ws.sum
    if (total <= 0.0)
      throw new IllegalArgumentException("Sum of weights must be positive.")

    // build prefix sums
    val prefixSums = ws
      .scanLeft(0.0)(_ + _)
      .tail                              // drop the 0.0 at head
      .toVector                          // Vector for fast random access
    new WeightedSampler(vs.toVector, prefixSums, total, rng)
  }
}
