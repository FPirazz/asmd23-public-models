package scala.u06.task2

import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}
import scala.u06.examples.task1.{ReadersWritersLiveness, ReadersWritersMutualExclusion}

class PNReadersWritersTask2Test extends AnyFunSuite:
  import scala.u06.examples.task1.PNReadersWriters.*

  test("Test the PN for Reader and Writers, considering the safety properties of Boundedness and Mutex"):
    val visitedMarkings = scala.collection.mutable.Set[MSet[Place]]()

    // The value for boundedness can be change whenever we want
    val liveness = ReadersWritersLiveness(Place.ReaderReady, Place.ReaderRunning)

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
          path.exists(state => mutualExclusion.isViolated(state) || liveness.isViolated(state))
        }
      }
    }

    val violations = Await.result(Future.sequence(violationFutures), Duration(10, MINUTES)).flatten

    assert(violations.isEmpty, s"Found ${violations.size} violation(s). They are: ${violations}")