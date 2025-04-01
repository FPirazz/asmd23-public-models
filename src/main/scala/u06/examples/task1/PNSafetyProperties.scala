package scala.u06.examples.task1

import u06.utils.MSet

import scala.u06.examples.task1.PNReadersWriters.*

trait PNSafetyProperties[S]:
  def isViolated(state: S): Boolean

case class ReadersWritersMutualExclusion[P](writerRunning: P, readerRunning: P) extends PNSafetyProperties[MSet[P]]:
  override def isViolated(state: MSet[P]): Boolean =
    val writerCount = state(writerRunning)
    val readerCount = state(readerRunning)
    writerCount > 1 || (writerCount > 0 && readerCount > 0)

case class Boundedness(maxTokens: Int) extends PNSafetyProperties[MSet[Place]]:
  def isViolated(state: MSet[Place]): Boolean =
    state.asMap.values.exists(_ > maxTokens)