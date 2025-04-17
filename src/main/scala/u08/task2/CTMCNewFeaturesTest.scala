package scala.u08.task2

object CTMCNewFeaturesTest:
  
  @main def testFeatures() = 
  
    import u08.examples.StochasticChannel
    import scala.u08.task2.CTMCAnalysis.*
    
    val ctmc = StochasticChannel.stocChannel
  
    // G formula: Check if the system is never in the FAIL state within 10 time units
    val gResult = ctmc.globally(
      runs = 1000,
      prop = _ != StochasticChannel.State.FAIL,
      s0 = StochasticChannel.State.IDLE,
      timeBound = 10.0
    )
    println(s"Probability of globally avoiding FAIL: $gResult")
  
    // Steady-state computation: Proportion of time spent in the DONE state
    val steadyStateResult = ctmc.steadyState(
      runs = 1000,
      s0 = StochasticChannel.State.IDLE,
      targetState = StochasticChannel.State.DONE,
      totalTime = 1000.0
    )
    println(s"Steady-state probability of DONE: $steadyStateResult")