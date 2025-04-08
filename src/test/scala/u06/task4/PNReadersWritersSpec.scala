package scala.u06.task4

import org.scalacheck.*
import org.scalacheck.*
import org.scalacheck.Prop.forAll
import u06.utils.MSet

import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.u06.examples.task1.{ReadersWritersBoundedness, ReadersWritersMutualExclusion}

object PNReadersWritersSpec extends Properties("PNReadersWriters") {

  import scala.u06.examples.task1.PNReadersWriters._

  val initialState = MSet(ProcessIdle, ProcessIdle)
  val maxDepth = 100
  val boundedness = ReadersWritersBoundedness(10)
  val mutualExclusion = ReadersWritersMutualExclusion(Place.WriterRunning, Place.ReaderRunning)

  property("boundedness and mutual exclusion") = forAll { (depth: Int) =>
    val visitedMarkings = scala.collection.mutable.Set[MSet[Place]]()
    val paths = pnRW.completePathsUpToDepthFiltered(initialState, depth, path => {
      val lastMarking = path.last
      if (visitedMarkings.contains(lastMarking)) false
      else {
        visitedMarkings += lastMarking
        true
      }
    })

    val violationFutures = paths.grouped(500).map { pathGroup =>
      Future {
        pathGroup.filter { path =>
          path.exists(state => mutualExclusion.isViolated(state) || boundedness.isViolated(state))
        }
      }
    }

    val violations = Await.result(Future.sequence(violationFutures), Duration(10, MINUTES)).flatten

    violations.isEmpty
  }
}