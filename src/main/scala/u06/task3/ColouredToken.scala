package scala.u06.task3

import u06.utils.MSet

case class ColouredToken[P, C](place: P, color: C)

type ColouredMarking[P, C] = MSet[ColouredToken[P, C]]