package scala.u07.task3

import u07.modelling.SPN.*
import u07.utils.MSet
import u07.modelling.SPN

object SPNBrusselator:

  enum Species:
    case X, Y
  
  // Choose your rate constants
  val A  = 1.0    // constant injection of X
  val k2 = 1.0    // degradation rate for X
  val k3 = 1.0    // rate constant for the autocatalytic step
  val B  = 3.0    // conversion rate from X to Y
  
  // Transition definitions:
  
  // 1. Injection: ∅ → X
  val injectX = Trn[Species](
    cond = MSet(),                // no reactants needed
    rate = _ => A,                // constant rate
    eff  = MSet(Species.X),       // add an X
    inh  = MSet()                // no inhibition
  )
  
  // 2. Degradation: X → ∅
  val degradeX = Trn[Species](
    cond = MSet(Species.X),       // need one X
    rate = m => k2 * m(Species.X),// rate proportional to the number of X tokens
    eff  = MSet(),                // removes X (since tokens in cond are extracted)
    inh  = MSet()
  )
  
  // 3. Autocatalysis: 2X + Y → 3X
  val autocatalysis = Trn[Species](
    // Need 2 tokens for X and 1 for Y.
    cond = MSet(Species.X, Species.X, Species.Y),
    // Rate proportional to the number of combinations of 2 X's and the available Y's.
    // Note: using the combinatorial factor explicitly, with binomial(n,2) = n*(n-1)/2.
    rate = m => k3 * (m(Species.X) * (m(Species.X) - 1) / 2.0) * m(Species.Y),
    // Produce 3 X tokens. Since the extraction removed 2 X's, this is a net addition of one X;
    // and Y is consumed.
    eff  = MSet(Species.X, Species.X, Species.X),
    inh  = MSet()
  )
  
  // 4. Conversion: X → Y
  val convert = Trn[Species](
    cond = MSet(Species.X),       // need one X
    rate = m => B * m(Species.X),  // conversion rate proportional to available X tokens
    eff  = MSet(Species.Y),       // produces a Y (X is consumed)
    inh  = MSet()
  )
  
  // The previous transitions could've been easily defined also inside the SPN itself, I did them separately
  // for clarity
  
  val spnBrusselator = SPN[Species](
    injectX,
    degradeX,
    autocatalysis,
    convert
  )
  
  @main def testRun =
    import u07.modelling.CTMCSimulation.*
    
    val initialMarking = MSet.ofList(List.fill(10)(Species.X)) union MSet.ofList(List.fill(10)(Species.Y))
    
    println(
      toCTMC(spnBrusselator).newSimulationTrace(initialMarking, new java.util.Random)
        .take(20)
        .toList.mkString("\n")
    )
