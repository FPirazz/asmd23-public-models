package scala.u06.examples.task1

export u06.modelling.PetriNet
import u06.utils.MSet

import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object PNReadersWriters:

  enum Place:
    case ProcessIdle, ProcessReady, ReaderReady, WriterReady, ReaderRunning, WriterRunning
    
  export Place.*
  export u06.modelling.PetriNet.*
  export u06.modelling.SystemAnalysis.*
  export u06.utils.MSet

  // DSL-like specification of a Petri Net that models Readers/Writers as per Slides
  def pnRW = PetriNet[Place](
    MSet(ProcessIdle) ~~> MSet(ProcessReady),

    MSet(ProcessReady) ~~> MSet(WriterReady),
    MSet(ProcessReady) ~~> MSet(ReaderReady),
    
    // The rule below is set to guarantee mutex from Readers
    MSet(ReaderReady) ~~> MSet(ReaderRunning) ^^^ MSet(WriterRunning),
    MSet(ReaderRunning) ~~> MSet(ProcessIdle),

    // This one instead to guarantee mutex from Writers, AND to have only 1 writer at a time
    MSet(WriterReady) ~~> MSet(WriterRunning) ^^^ MSet(WriterRunning, ReaderRunning),
    MSet(WriterRunning) ~~> MSet(ProcessIdle),
  ).toSystem

@main def mainPNReadersWriters =
  import PNReadersWriters.*
  // example usage
//  println(pnRW.paths(MSet(ProcessIdle, ProcessIdle),7).toList.mkString("\n"))

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

  if (violations.isEmpty)
    println("No violations found in paths of length up to 100")
  else
    println(s"Found ${violations.size} violation(s).")
    println(s"They are: ${violations}")
