package u09.examples

import u09.model.QMatrix

object QMatrixZigZag extends App :

  import u09.model.QMatrix.Move.*
  import u09.model.QMatrix.*

  val rl: QMatrix.Facade = Facade(
    width = 5,
    height = 3,
    initial = (0,0),
    terminal = {
      case (1,4) => true;
      case _ => false;
    },
    reward = {
      case ((1,4), _) => 100;
      case ((x, y), _) if Set((0,1), (1,1), (1,3), (2,3)).contains((x, y)) => -100  // Penalità per incontrare ostacoli
      case ((x, y), _) if Set((2,0), (2,2), (0,2)).contains((x, y)) => 10  // Penalità per incontrare ostacoli
      case _ => -1
    },
    jumps = PartialFunction.empty,
    gamma = 0.9,
    alpha = 0.5,
    epsilon = 0.9,
    v0 = 1
  )

  val q0 = rl.qFunction
  println(rl.show(q0.vFunction,"%2.2f"))
  val q1 = rl.makeLearningInstance().learn(10000,1000,q0)
  println(rl.show(q1.vFunction,"%2.2f"))
  println(rl.show(s => q1.bestPolicy(s).toString,"%7s"))