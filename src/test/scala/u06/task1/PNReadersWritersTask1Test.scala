package scala.u06.task1

import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.u06.examples.task1.{ReadersWritersBoundedness, ReadersWritersMutualExclusion}

class PNReadersWritersTask1Test extends AnyFunSuite:
  import scala.u06.examples.task1.PNReadersWriters.*

  test("Test the PN for Reader and Writers, considering the safety properties of Boundedness and Mutex"):
    val visitedMarkings = scala.collection.mutable.Set[MSet[Place]]()

    // The value for boundedness can be change whenever we want
    val boundedness = ReadersWritersBoundedness(10)

    val mutualExclusion = ReadersWritersMutualExclusion(Place.WriterRunning, Place.ReaderRunning)
    val paths = pnRW.completePathsUpToDepthFiltered(MSet(ProcessIdle, ProcessIdle), 100, path => {
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

    assert(violations.isEmpty, s"Found ${violations.size} violation(s). They are: ${violations}")