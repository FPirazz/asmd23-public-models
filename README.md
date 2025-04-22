# 06Lab - System modelling, a Programming Language approach: non-determinism, unbounded size, Petri Nets

## Task 1: VERIFIER

Code and do some analysis on the Readers & Writers Petri Net. Add a test to check that in no path long at most 100 
states mutual exclusion fails (no more than 1 writer, and no readers and writers together). Can you extract a small API 
for representing safety properties? What other properties can be extracted? How the boundness assumption can help?

### Work Done:

The tasks asked for the implementation of a Petri Net regarding the Writers & Readers implementation in the slides, which
has been done in the class [PNReadersWriters.scala](src/main/scala/u06/examples/task1/PNReadersWriters.scala), where
the places enumerated are:
```scala 3
enum Place:
    case ProcessIdle, ProcessReady, ReaderReady, WriterReady, ReaderRunning, WriterRunning
```

And the DSL-like specs for building transitions is:
```scala 3
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
```
When it comes to the safety (& others) properties that are asked to be implemented and tested, they are implemented in
the [PNSafetyProperties.scala](src/main/scala/u06/examples/task1/PNSafetyProperties.scala) class, and used in the
previously mentioned PNReadersWriters class and also in the test class 
[PNReadersWritersTask1Test.scala](src/test/scala/u06/task1/PNReadersWritersTask1Test.scala). The properties that are 
checked for are:
* **Mutual Exclusion** (Mutex), in order to make sure that there are no processes that are simultaneously Reading *AND*
Writing at the same time;
* **Boundedness**, which check and verifies that the number of tokens in each place of the PN does not go over a certain
boundary (in the file executed it's set to 10).

Again referencing the first scala class, the implementation verifies the properties of Mutex and Boundedness for the 
newly PN model of Reader & Writers. The main abstraction that guarantees a better separation of implementations, is the
usage of generic case class for each ```"isVerified"``` property, that can be extended to check even more different
properties.

## Task 2: DESIGNER

Code and do some analysis on a variation of the Readers & Writers Petri Net: it should be the minimal variation you can 
think of, such that if a process says it wants to read, it eventually (surely) does so. How would you show evidence 
that your design is right? What about a variation where at most two process can write?

### Work Done:

As asked per the task, I reused the PN developed during the first task, and by doing very minimal changes I added a
property to make sure that, if a process wants to read, eventually it will. This can very much be represented 
through the liveness property which is usually present in concurrency systems.

Once again, the property has been added to the 
[PNSafetyProperties.scala](src/main/scala/u06/examples/task1/PNSafetyProperties.scala), amongst all the others already
implemented in the following way:
```scala 3
// Task 2
case class ReadersWritersLiveness[P](waitPlace: P, goalPlace: P) extends PNSafetyProperties[MSet[P]]:
  def isViolated(marking: MSet[P]): Boolean =
    marking(waitPlace) > 0 && marking(goalPlace) == 0
```
And it has been tested in the file
[PNReadersWritersTask2Test.scala](src/test/scala/u06/task2/PNReadersWritersTask2Test.scala), where, by changing one line,
instead of checking the boundedness property from the previous task, it now checks for the liveness on, using the same
structure used before to test and make sure the properties have been verified in paths that are, at most, not longer
than 100 states.

## Task 3: ARTIST

Create a variation/extension of PetriNet meta-model, with priorities: each transition is given a numerical priority,
and no transition can fire if one with higher priority can fire. Show an example that your pretty new “abstraction”
works as expected. Another interesting extension is “coloring”: tokens have a value attached, and this is read/updated
by transitions.

### Work Done:

To implement the work asked by the task, I created two extensions to the normal Petri Net model, one for the priority
system, and on for the coloring system. The first one is implemented in the file
[PetriNetPriority.scala](src/main/scala/u06/task3/PetriNetPriority.scala), meanwhile the second one is in the file
[ColouredPetriNet.scala](src/main/scala/u06/task3/ColouredPetriNet.scala) that extends
[ColouredToken.scala](src/main/scala/u06/task3/ColouredToken.scala).

#### **Priority System**

As it can be seen in the file, there has been an extensione of the base Petri Net class, which now has a priority as per
the Trn class. As also stated in the task, the priority is a numerical value, and when the transitions are fired, it is
given priority to the one with the highest value, therefore letting the user design a net where certain processes have
higher priority over others, which is done by ordering the transitions by their priority value, in descending order.

This has been tested in the file [PetriNetsPriorityTest.scala](src/test/scala/u06/task3/PetriNetsPriorityTest.scala),
where the example assumes that a PN exists wth 3 Places, and 2 transitions, where the first transition has a higher 
priority than the other. When, as tested towards the end of the file, the transition with the higher priority is fired, 
meanwhile the other is not.

#### **Coloring System**

A Colored Petri Net, or CPN, is a type of Petri net where tokens have "colors" (which can be thought of as data,
for example a simple integer value) that can be used to represent different types of tokens. Although the data can be of
arbitrarily complex types, places in CPN contain tokens of a single type.

Firstly the case class ```ColouredToken``` is defined, which represents a token with a color and a value.
The color is represented as a generic type, to include any type of data to be transported by the token, to let also
implement better a PN class to interact with these values.

Lastly then ```ColouredPetriNet``` is the class that extends the base Petri Net class, and it is used to create a 
colored Petri net, which contains as usual places and transitions, but also the colored tokens.

This instead has been tested in the file 
[ColouredPetriNetsTest.scala](src/test/scala/u06/task3/ColouredPetriNetsTest.scala), and as we can see, there's
multiple transitions that are composed of multiple places, and the tokens are colored with different values. The test
tries to fire a transition coming from the initial place defined, with a certain value to it, which then in execution
transits to the correct place.

## Task 4: TOOLER

The current API might be re-organised: can we generate/navigate all paths thanks to caching and lazy evaluation? Can 
we use monads/effects to capture non-determinism? Can we generate paths and capture safety properties by ScalaCheck?

### Work Done:

The implementation of the Cache and Lazy evaluation has been done in the files 
[CachedSystem.scala](src/main/scala/u06/task4/CachedSystem.scala) and 
[LazySystem.scala](src/main/scala/u06/task4/LazySystem.scala), which are then tested inside the file 
[PNExample.scala](src/main/scala/u06/task4/PNExample.scala).

### `CachedSystem` Class

The `CachedSystem` class is designed to add caching functionality to an existing `System`. This means that it stores the results of previous computations to avoid redundant calculations.

#### Key Points:
1. **Constructor**: It takes an existing `System` as a parameter.
2. **Cache**: It uses a mutable map to store the results of the `next` method for different states.
3. **Override `next` Method**: The `next` method checks if the result for a given state is already in the cache. If it is, it returns the cached result. If not, it computes the result using the underlying system, stores it in the cache, and then returns it.

```scala
package scala.u06.task4

import scala.collection.mutable
import u06.modelling.System

class CachedSystem[S](system: System[S]) extends System[S] {
  private val cache = mutable.Map[S, Set[S]]()

  override def next(a: S): Set[S] = {
    cache.getOrElseUpdate(a, system.next(a))
  }
}
```

### `LazySystem` Class

The `LazySystem` class is designed to add lazy evaluation functionality to an existing `System`. This means that it defers the computation of transitions until they are actually needed.

#### Key Points:
1. **Constructor**: It takes an existing `System` as a parameter.
2. **Lazy Cache**: It uses a mutable map to store functions that compute the results of the `next` method for different states.
3. **Override `next` Method**: The `next` method checks if a function for computing the result for a given state is already in the lazy cache. If it is, it calls the function to get the result. If not, it creates a function that computes the result using the underlying system, stores this function in the lazy cache, and then calls the function to get the result.

```scala
package scala.u06.task4

import u06.modelling.System
import scala.collection.mutable

class LazySystem[S](system: System[S]) extends System[S] {
  private val lazyCache = mutable.Map[S, () => Set[S]]()

  override def next(a: S): Set[S] = {
    lazyCache.getOrElseUpdate(a, () => system.next(a))()
  }
}
```

# 07Lab - Stochastic modelling: uncertainty, probability and frequencies


## Task 1: SIMULATOR
Take the communication channel CTMC example in StochasticChannelSimulation. Compute the average time at which 
communication is done—across n runs. Compute the relative amount of time (0% to 100%) that the system is in fail state 
until communication is done—across n runs. Extract an API for nicely performing similar checks.

### Work Done:

The goal is to compute key metrics related to the system's behavior and provide a reusable API for performing similar 
analyses on other CTMC models. Specifically, the following tasks have been accomplished:

1. **Compute the Average Time to Completion**: Calculate the average time at which the communication process reaches the 
*DONE* state across *n* simulation runs.
2. **Compute the Relative Time in the FAIL State**: Determine the percentage of time the system spends in the *FAIL* state 
relative to the total time until the communication process reaches the DONE state, averaged across *n* simulation runs.
3. **Extract a Reusable API**: Develop a generic and extensible API (SimulationAnalysis) to perform similar checks on any 
CTMC model, decoupling the analysis logic from the specific StochasticChannel example.
<hr></hr>

#### Key Components

**Simulation Analysis API**

The SimulationAnalysis object provides a reusable API for analyzing CTMC simulations. It includes:


* **averageTimeToState**: Computes the average time to reach a specific state (e.g., DONE) across multiple simulation runs.
* **relativeTimeInState**: Computes the relative time spent in a specific state (e.g., FAIL) until another state (e.g., DONE) is reached, averaged across multiple runs.
The API is designed to be generic, allowing it to work with any CTMC model by accepting a trace generator function as input.

<hr></hr>

#### Implementation Details

**1. Average Time to Completion**

The *averageTimeToState* method calculates the average time at which the system reaches a target state (_endState_) 
across _n_ runs. It works as follows:

* For each run, the simulation trace is generated starting from the initial state.
* The method identifies the first event where the system reaches the target state and records the time.
* The average time is computed by summing the recorded times and dividing by the number of runs.

**2. Relative Time in the _FAIL_ State**
   
The _relativeTimeInState_ method calculates the percentage of time the system spends in a specific state (_targetState_) 
relative to the total time until another state (_endState_) is reached. It works as follows:

* For each run, the simulation trace is generated starting from the initial state.
* The method calculates the total time and the time spent in the target state by iterating over the trace.
* The ratio of the target time to the total time is computed for each run, and the average ratio is returned.

**3. Reusable API**
   
The _SimulationAnalysis_ API is implemented with modularity and reusability in mind:

* A helper method, _runSimulations_, encapsulates the logic for running multiple simulations and processing their traces.
* The analysis methods (_averageTimeToState_ and _relativeTimeInState_) are concise and focus on their specific 
computations, delegating common tasks to the helper method.

## Task 2: GURU
Check the SPN module, that incorporates the ability of CTMC modelling on top of Petri Nets, leading to Stochastic 
Petri Nets. Code and simulate Stochastic Readers & Writers shown in previous lesson. Try to study how key 
parameters/rate influence average time the system is in read or write state.

### Work Done:

**Stochastic Petri Nets (SPN)**

SPNs extend Petri Nets by associating stochastic rates with transitions. These rates define the probability of a 
transition firing over time, enabling the modeling of systems with probabilistic behavior. SPNs are particularly useful 
for performance analysis and reliability studies. The work done can be seen in the file 
[StochasticReadersWriters.scala](src/main/scala/u07/task2/StochasticReadersWriters.scala).

**Continuous-Time Markov Chains (CTMC)**

SPNs are mapped to CTMCs, where:

* States correspond to markings (token distributions) in the Petri Net.
* Transitions correspond to state changes with associated rates.

#### **Key Components**

_**PNReadersWriters**_

This module implements a Petri Net for the Readers-Writers problem, ensuring:

* Mutual Exclusion: Readers and writers cannot access the shared resource simultaneously.

**_StochasticReadersWriters_**

This module extends the Readers-Writers problem to a _Stochastic Petri Net_ by:

* Assigning stochastic rates to transitions.
* Simulating the system using CTMCs to analyze its behavior over time.

Key features include:

* **Transition Rates**: Rates are defined as functions of the current marking, allowing dynamic behavior.
* **CTMC Simulation**: The SPN is converted to a CTMC, and simulation traces are generated to study the system's evolution.

**Simulation and Analysis**

The project provides tools to:

1. Simulate the Readers-Writers problem using both deterministic and stochastic models.
2. Analyze the impact of transition rates on the system's performance, such as:
   * Average time spent in the read or write state. 
   * Frequency of state transitions. 
3. Detect violations of mutual exclusion or boundedness in the deterministic model.


#### **Key Insights**

**Stochastic Model**

* Adds probabilistic behavior to the system.
* Allows studying the impact of transition rates on system performance.
* Provides insights into the average time spent in different states.

// TODO Add information related to running several tests to gather average speed

## Task 3: CHEMIST
SPNs can be used to simulate dynamics of chemical reactions. Experiment with it. E.g.: search the “Brussellator” 
chemical reaction on wikipedia: it oscillates! Try to reproduce it.

### Work Done:

#### **SPN Simulation of the Brusselator Chemical Reaction**

The SPNs can be used to simulate the dynamics of chemical reactions, and in this example, we model the 
classic `Brusselator`, a chemical oscillator, using the availabel SPN framework in the project lab. The Brusselator 
exhibits oscillatory behavior under certain parameter regimes, and this simulation demonstrates how to capture such 
dynamics using a stochastic approach.

**Overview**

The Brusselator is described by a set of chemical reactions that can be formulated as:

1. **Injection:** <br> 
∅ → X <br>
Produces species X at a constant rate.


2. **Degradation:** <br>
X → ∅ <br>
Degrades species X at a rate proportional to its current number.


3. **Autocatalytic Reaction:** <br>
2X + Y → 3X <br>
Uses 2 tokens of X and 1 token of Y to produce 3 tokens of X. This reaction converts a Y into an additional X (net 
effect), and is responsible for non-linear dynamics (oscillations).


4. **Conversion Reaction:** <br>
X → Y <br>
Converts species X into species Y at a rate proportional to the available X.

This simulation represents each reaction as an SPN transition. The transitions are defined by a condition (the tokens 
or reactants required), a rate function (using mass-action kinetics), an effect (the tokens added or produced), and an 
optional inhibition set (not used for these reactions).

The SPN is then converted to a Continuous-Time Markov Chain (CTMC) which allows us to simulate the stochastic dynamics 
of the system.

Additionally, in many stochastic formulations A and B are treated as constant parameters that define the external 
feed and conversion rate. One can usually either assume A and B are “external” (i.e. not represented as tokens) or 
introduce tokens that remain constant. For simplicity, I included only the dynamic species X and Y in the net.

#### **Code Structure**

##### **Modeling the Brusselator**
1. Defining the Species <br>
   We define an enumeration to represent the two species in the Brusselator:

```scala 3
enum Species:
   case X, Y
```

2. Creating the Transitions <br>
Each reaction of the Brusselator is implemented as an SPN transition:

   * **Injection of X:** <br>
   No prerequisites, constant rate A.

   * **Degradation of X:** <br>
   Requires one token of X. The rate is proportional to the number of X tokens.

   * **Autocatalytic Reaction:** <br>
   Requires two tokens of X and one token of Y. Its rate is proportional to the combinatorial factor (choosing 2 X’s) 
   times the number of Y tokens. After firing, it produces three tokens of X (resulting in a net addition of one X as 
   one Y is consumed).
   
   * **Conversion Reaction:** <br>
   Requires one token of X and converts it to one token of Y, with the rate proportional to the number of X tokens.

A sketch of the transition definitions is as follows:

```scala 3
   // Choose your rate constants
   val A  = 1.0    // constant injection of X
   val k2 = 1.0    // degradation rate for X
   val k3 = 1.0    // rate constant for the autocatalytic step
   val B  = 3.0    // conversion rate from X to Y
   
   // Transition definitions:
   
   // 1. Injection: ∅ → X
   val injectX = Trn[Species](
   cond = MSet(),                
   rate = _ => A,                
   eff  = MSet(Species.X),       
   inh  = MSet()                
   )
   
   // 2. Degradation: X → ∅
   val degradeX = Trn[Species](
   cond = MSet(Species.X),      
   rate = m => k2 * m(Species.X),
   eff  = MSet(),                
   inh  = MSet()
   )
   
   // 3. Autocatalysis: 2X + Y → 3X
   val autocatalysis = Trn[Species](
   cond = MSet(Species.X, Species.X, Species.Y),
   rate = m => k3 * (m(Species.X) * (m(Species.X) - 1) / 2.0) * m(Species.Y),
   eff  = MSet(Species.X, Species.X, Species.X),
   inh  = MSet()
   )
   
   // 4. Conversion: X → Y
   val convert = Trn[Species](
   cond = MSet(Species.X),       
   rate = m => B * m(Species.X), 
   eff  = MSet(Species.Y),       
   inh  = MSet()
   )
```


3. Assembling the SPN and Simulation <br>
   Combine the transitions into an SPN:

```scala 3
   val spnBrusselator = SPN[Species](
     injectX,
     degradeX,
     autocatalysis,
     convert
   )
```

Choose an initial marking—for example, 10 tokens for species X and 5 tokens for species Y:

```scala 3
  val initialMarking = MSet.ofList(List.fill(10)(Species.X)) union MSet.ofList(List.fill(5)(Species.Y))
```

Finally, convert the SPN to a CTMC and simulate a trace:

```scala 3
    println(
      toCTMC(spnBrusselator).newSimulationTrace(initialMarking, new java.util.Random)
        .take(20)
        .toList.mkString("\n")
    )
```

## Task 4: RANDOM-UNIT-TESTER
How do we unit-test with randomness? And how we test at all with randomness? Think about this in general. Try to create 
a repeatable unit test for Statistics as in utils.StochasticSpec.

### Work Done:

#### Strategies for Testing Randomness

### 1. Deterministic Testing with a Stubbed Random Generator

One way to overcome the non-determinism of randomness is by "controlling" the `Random` number generator. By injecting a 
custom or stubbed random generator that produces a predefined sequence of values, you can ensure that each call to a 
method (like `Stochastics.draw`) behaves predictably. This approach makes your tests completely repeatable.

#### How it Works

- **Stub Implementation:** Create a custom `StubRandom` class that extends Scala's `Random` and returns values from a 
fixed sequence.
- **Deterministic Outcome:** For example, using a known value (e.g., 0.2), you can force the `draw` function to return 
a specific result based on the cumulative probabilities.

### 2. Statistical Testing Over Multiple Iterations

For testing the fairness or the statistical properties of the randomness, you can run a large number of iterations and 
then compare the resulting distribution against the expected probabilities. This method allows you to check that the 
function behaves correctly on average.

#### How it Works

- **Large Sample Size:** Run the random function (e.g., `statistics`) for many iterations (e.g., 10,000 times).
- **Tolerance Checks:** Use assertions with a tolerance range (e.g., using ScalaTest’s `+-`) to ensure the observed 
frequencies are close to the expected probabilities.

## Reference Code Examples

### Deterministic Test Using a Stubbed Random Generator

Below is an example test where a custom `StubRandom` class controls the randomness, ensuring that the `draw` method 
yields a predictable result, which can be found also in the file 
[StatisticalStochasticsTest.scala](src/test/scala/u07/task4/StatisticalStochasticsTest.scala).

```scala
import org.scalatest.funsuite.AnyFunSuite
import scala.util.Random
import u07.utils.Stochastics

// A stubbed Random that returns a pre-defined sequence of doubles
class StubRandom(seq: Seq[Double]) extends Random {
  private val iterator = seq.iterator
  override def nextDouble(): Double = if (iterator.hasNext) iterator.next() else 0.0
}

// Deterministic unit test using the stubbed Random
class DeterministicStochasticsTest extends AnyFunSuite {

  test("draw should return expected value with stubbed random") {
    // For example, if we know that using 0.2 as the random value makes draw return "b"
    val stubRandom = new StubRandom(Seq(0.2))
    implicit val rnd: Random = stubRandom

    val choices = List(1.0 -> "a", 2.0 -> "b", 3.0 -> "c")
    val cumulativeList = Stochastics.cumulative(choices)
    
    // Here, 0.2 * totalSum (6.0) gives 1.2; with cumulative list:
    // List((1.0, "a"), (3.0, "b"), (6.0, "c"))
    // Since 1.0 < 1.2 <= 3.0, the expected draw is "b".
    val drawn = Stochastics.draw(cumulativeList)
    
    assert(drawn == "b")
  }
}
```

# 08Lab - SStochastic analysis: probabilistic/continuous-stochastic logic from model checking to simulation

## Task 1: PRISM

* Make the stochastic Readers & Writers Petri Net seen in lesson work: perform experiments to investigate the probability
  that something good happens within a bound
* Play with PRISM configuration to inspect steady-state proabilities of reading and writing (may need to play with options
  anche choose “linear equations method”)

## Work Done:

### Model Checking the Stochastic Readers & Writers Problem

The Readers & Writers problem is modeled as a Continuous-Time Markov Chain (CTMC) in PRISM. In this model, the states 
and transitions represent various conditions such as:

- **`p1`**: A count variable indicating available resources.
- **`p2`, `p3`, `p4`, `p5`, `p6`, `p7`**: Variables representing different phases or actors (e.g., waiting readers, 
- writing, active reading, etc.).

The code used for PRISM is the same one as the one presented in the lab, as follows:
```
ctmc

const int N = 20;

module RW
p1 : [0..N] init N;
p2 : [0..N] init 0;
p3 : [0..N] init 0;
p4 : [0..N] init 0;
p5 : [0..N] init 1;
p6 : [0..N] init 0;
p7 : [0..N] init 0;

[t1] p1>0 & p2<N -> 1 : (p1'=p1-1)&(p2'=p2+1);
[t2] p2>0 & p3<N -> 200000 : (p2'=p2-1) & (p3'=p3+1);
[t3] p2>0 & p4<N -> 100000 : (p2'=p2-1) & (p4'=p4+1);
[t4] p3>0 & p5>0 & p6<N -> 100000 : (p3'=p3-1) & (p6'=p6+1);
[t5] p4>0 & p5>0 & p6=0 & p7<N -> 100000 : (p4'=p4-1) & (p5'=p5-1) & (p7'=p7+1);
[t6] p6>0 & p1<N -> p6*1 : (p6'=p6-1) & (p1'=p1+1);
[t7] p7>0 & p5<N & p1<N -> 0.5 : (p7'=p7-1) & (p1'=p1+1) & (p5'=p5+1);

endmodule
```

Where the example query presented to test is this one:

``` P=? [(true) U<=k (p6>0)]```

Where the query meaning can be translated as: 
> "What’s the probability that we eventually reach a state where someone is reading (p6 > 0) within k time units?"

The model has been tested and verified and PRISM (and both function correctly), then thereafter, this considered, I tested
the configuration of PRISM to enable steady-state probabilities (for the same model), which I did via GUI through the
"Options" tab, then "Options" again, and once the window showed up, I choose the `Explicit` engine, which, as stated on
the PRISM website:
```
The explicit engine is similar to the sparse engine, in that it can be a good option for relatively small models, 
but will not scale up to some of the models that can be handled by the hybrid or MTBDD engines. However, unlike the 
sparse engine, the explicit engine does not use symbolic data structures for model construction, which can be 
beneficial in some cases. One example is models with a potentially very large state space, only a fraction of which 
is actually reachable.
```

### Steady-state Probabilities

As per requested task, I also tried to play around and test with the previously mentioned configuration in order to
inspect Steady-State Probabilities (SSP) for the previous Reader & Writers model.

The properties I tested for the SSP of the model were the following:
* ```S=? [ p6 > 0 ]``` 
* ```S=? [ p7 > 0 ]```

Once the queries and configurations were set, I:

* **Run the Analysis**: Execute your model against the queries for steady-state probability determination.

* **Interpretation**:
  * `S=? [ p6 > 0 ]`: Indicates the probability that, in the long run, the system has at least one active reader. 
  * `S=? [ p7 > 0 ]`: Indicates the probability that, in the long run, the system has an active writer.

These results help in understanding how frequently the system is reading or writing over an extended period, providing 
insight into performance, resource utilization, or potential bottlenecks.

The results, obtained through verification given of the queries, were the following:

* For the first query, the result (probability) was of **0.31814921752052894**;
* Meanwhile for the second, the result (probability) was of **0.6417386441223224**.

## Task 2: PRISM-VS-SCALA

* Take the communication channel example, and perform comparison of results between PRISM and our Scala approach
* Write Scala support for performing additional experiments and comparisons (e.g., G formulas, steady-state 
computations)

## Work Done:

Both versions of the Communication Channel model, work correctly and give out the expected results, obviously the
Scala one was already implemented in the given Lab, meanwhile the PRISM one was tested through the material given
and/or the slides seen during class. I've found the PRISM one all in all to be faster to execute and implement, given
obviously the nature of the application, especially considering that we can use different "engines" as stated in the
previous task, to change the way experiments/calculations/verifications are done.


### Additional Scala Support for Experiments and Comparisons

In this second part, it regards the extension of the CTMC framework already present to support additional experiments 
and comparisons, such as **Globally (G) formulas** and **Steady-State Computations**. These features allow for advanced 
analysis of stochastic models, similar to PRISM, but obviously implemented in Scala.

This part includes:
1. **Globally (G) Formulas**: Verifying if a property holds globally within a time bound.
2. **Steady-State Computations**: Approximating the proportion of time spent in a given state.
3. **Reusable API**: A modular and extensible API for performing these analyses on any CTMC model.

### Key Features

#### 1. Globally (G) Formulas
The `G` formula checks if a property holds globally (i.e., at all times) within a given time bound. This is useful for 
verifying safety properties, such as ensuring that a system never enters a failure state within a specific time frame.

**Implementation**:
- Simulates multiple traces of the CTMC.
- For each trace, checks if the property holds for all states within the time bound.
- Returns the proportion of traces where the property holds globally.

This is done in the [CTMCAnalysis.scala](src/main/scala/u08/task2/CTMCAnalysis.scala) file.

#### 2. Steady-State Computations
Steady-state probabilities are approximated by running long simulations and observing the proportion of time spent in 
each state. This is useful for analyzing the long-term behavior of a system.

**Implementation**:
- Simulates multiple traces of the CTMC.
- For each trace, calculates the total time spent in the target state as a proportion of the total simulation time.
- Averages the results across all runs.

Again, this is implemented in the [CTMCAnalysis.scala](src/main/scala/u08/task2/CTMCAnalysis.scala) file.

#### 3. Reusable API
The API is designed to be generic and reusable, allowing it to work with any CTMC model. It provides methods for 
performing the above analyses and can be easily extended for additional experiments.

### Code Structure

#### `CTMCAnalysis.scala`
This file contains the core implementation of the new features. It defines extension methods for the `CTMC` trait to 
support globally formulas and steady-state computations.

##### Key Methods:
- **`globally`**: Computes the probability that a property holds globally within a time bound.
- **`steadyState`**: Computes the steady-state probability of a given state.

#### `CTMCNewFeaturesTest.scala`
This file demonstrates how to use the new features with an example CTMC model. It includes tests for:
- Checking if a system avoids a failure state globally within a time bound.
- Calculating the steady-state probability of a specific state.

#### Example Usage
The following example demonstrates how to use the new features with a stochastic communication channel model:

```scala
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
```

### How It Works

#### Globally (G) Formula

1. Simulate multiple traces of the CTMC starting from the initial state.
2. For each trace, iterate through the events and check if the property holds for all states within the time bound.
3. Count the number of traces where the property holds globally and divide by the total number of runs.

#### Steady-State Computation

1. Simulate multiple traces of the CTMC starting from the initial state.
2. For each trace, calculate the total time spent in the target state and the total simulation time.
3. Compute the ratio of the target time to the total time for each trace.
4. Average the ratios across all runs.

### Results

#### Example Results

Using the stochastic communication channel model:


* Globally Formula: The probability of avoiding the `FAIL` state globally within 10 time units was approximately `0.69`.
* Steady-State Computation: The steady-state probability of being in the `DONE` state was approximately `0.99`.

These results demonstrate the effectiveness of the new features in analyzing CTMC models.

## Task 3: LARGE-SCALE-DESIGN

* Get acquainted with the simulations and the code for DAP and DAPGossip
* Implement and simulate a system where gossip-like broadcasts are used to send a message and get a reply from a node
* Can you perform a statistical experiment to estimate time of reply?

## Work Done:


## Overview

This task focuses on the analysis of Distributed Asynchronous Petri Nets (DAPs) and their extensions to model stochastic
systems. The work includes creating a gossip-based request-reply system, performing statistical experiments, and 
analyzing the results using Continuous-Time Markov Chains (CTMCs).

---

### Implementation 1: Gossip-Based Request-Reply System

#### **Objective**
Implement and simulate a system where gossip-like broadcasts are used to send a message and receive a reply from a node.

#### **Implementation**
1. **Modeling the System**:
    - The system is modeled using a DAP with three states: `REQUEST`, `REPLY`, and `IDLE`.
    - Rules define the transitions between these states:
        - `REQUEST` → `REPLY`
        - `REPLY` → `IDLE`

2. **Simulation**:
    - The DAP is converted into a CTMC for stochastic simulation.
    - A grid-based network is used to represent the nodes.

3. **Code**:
   The implementation is in the file `src/main/scala/u08/task3/GossipRequestReply.scala`, or 
[here](src/main/scala/u08/task3/GossipRequestReply.scala).

---

### Task 2: Statistical Experiment to Estimate Reply Time

#### **Objective**
Perform a statistical experiment to estimate the average time it takes to receive a reply in the gossip-based system.

#### **Implementation**
1. **Experiment Setup**:
    - Simulate the system multiple times (e.g., 1000 runs).
    - For each simulation, record the time it takes for the system to transition from `REQUEST` to `IDLE`.

2. **Statistical Analysis**:
    - Compute the average time across all runs.
    - Handle edge cases where no reply is received by assigning `Double.PositiveInfinity`.

3. **Code**:
   The experiment is implemented in the file `src/main/scala/u08/task3/StatisticalExperiment.scala`, or 
[here](src/main/scala/u08/task3/StatisticalExperiment.scala).

4. **Key Method**:
    - `estimateReplyTime(runs: Int): Double`: Simulates the system and calculates the average reply time.

---

### Task 3: Fixes and Enhancements

#### **Challenges Addressed**
1. **Type Inference Issues**:
    - Fixed the issue where the compiler treated `state` as `Any` by explicitly typing it as `State[ID, Place]`.
    
2. **Pattern Matching Errors**:
    - Adjusted the pattern matching to use the `Event` case class instead of tuples.

3. **MSet Compatibility**:
    - Resolved the issue with `exists` by converting `MSet` to a list using `asList`.

#### **Key Fixes**
- Updated the `takeWhile` clause to:
  ```scala
  case Event(_, state: State[ID, Place]) =>
    !state.tokens.asList.exists(_.p == Place.IDLE)
  ```

---

### Task 4: Insights and Results

#### Simulation Results
* The system successfully transitions from `REQUEST` to `IDLE` in all valid runs.
* The average reply time is computed and printed in the runExperiment method, around `1 second` given the default set-up.



# 09Lab - Reinforcement Learning: from MDP to RL and Q-Learning, Deep Q-Learning, and MARL

## Task 1: BASIC-Q-LEARNING

* Get acquainted with the basic tool of Q-learning, focussing on examples/TryQLearningMatrix
* check how variation of key parameters (epsilon, gamma, alpha, episode length) affects learning
* check how learning gets more difficult as the grid size increases

## Work Done:

After checking that the basic example given inside 
[TryQLearningMatrix.scala](src/main/scala/u09/examples/TryQLearningMatrix.scala) works, I started to observer and
experiment with the parameters given, and how they affect the learning process of the Q-learning algorithm.
As per task, I experimented with the **epsilon**, **gamma**, **alpha** and **episode length** parameters.

* ### Epsilon:
The **epsilon** parameter controls the exploration-exploitation trade-off in the Q-learning algorithm. A higher epsilon 
value means exploration is encouraged, which is useful in the early stage of learning. A lower epsilon value means the 
agent favors exploitation of the learned policy. 

Here below we can see some of the experimentation done with the parameter, where initially I just used the base example
given and modified the parameter (Below side by side are outputs where epsilon was firstly = 0.3, as per default, 
and then = 0.9):

| ![epsilon1.png](READMEimages/epsilon/epsilon1.png) | ![epsilon2.png](READMEimages/epsilon/epsilon2.png) |
|-----------------------------------------------|-----------------------------------------------------|

As we can see, the results are the same, this maninly comes from the fact that the grid to explore is very small, as it
is of size 5x5, and, as a matter of fact, by just increasing the size to a 10x10, we can already see the effects of the
parameter below (re-using the same epsilon values as before):

|![epsilon3.png](READMEimages/epsilon/epsilon3.png) | ![epsilon4.png](READMEimages/epsilon/epsilon4.png)  |
|-----------------------------------------------|-----------------------------------------------------|

It's very clear from the images, that the second one (with epsilon = 0.9) is much more exploratory, and the agent has 
fully locked in, in using the first jump, the one that has a larger reward, since it has better explored the environment,
meanwhile the second still struggles to properly navigate the environment, as it can be seen in the lower part of graph 
of the first image.

* ### Gamma:
The **gamma** parameter determines the discount factor for future rewards. A higher gamma value means the agent 
considers long term rewards more and prioritizes them, meanwhile is exactly the opposite for a lower gamma value,
meaning short term rewards are prioritized.

Again below, more experimentation has been done, firstly with the default setup, where in the first image the gamma
values was set to 0.9, as per default, and in the second to 0.1:

| ![gamma1.png](READMEimages/gamma/gamma1.png) | ![gamma2.png](READMEimages/gamma/gamma2.png) |
|----------------------------------------------|----------------------------------------------|

As we can see, the first image makes absolute use of the first jump, which is the one with the highest reward, meanwhile
the second image, with gamma = 0.1, is much shorter term oriented, and when available based on position, uses the second
jump more often, which is the one with the lower reward.

Further testing has been done with a larger grid, with the same gamma values per image, and also with the second jump 
moved further from the first, more precisely to the coordinate (6, 0), where the destination coordinates are (6, 7), to 
further illustrate the previously explained point, and the results are as follows:

| ![gamma3.png](READMEimages/gamma/gamma3.png)| ![gamma4.png](READMEimages/gamma/gamma4.png) |
|----------------------------------------------|----------------------------------------------|

Once again, the previously stated observations can also be seen here.

* ### Alpha:
The **alpha** parameter determines the learning rate of the Q-learning algorithm. A higher alpha value means that 
faster updates to the Q-values are allowed, but it may lead to instability, meanwhile lower values result in slower
learning but more stable convergence.

Once again, the experiments below show the results of the alpha parameter, where in the first image the alpha value was 
set to 0.2, and then to 0.9, to show the convergence difference:

| ![alpha1.png](READMEimages/alpha/alpha1.png) | ![alpha2.png](READMEimages/alpha/alpha2.png) |
|----------------------------------------------|----------------------------------------------|

In the 5x5 grid, the difference is not noticeable unfortunately, but in the 10x10 grid, as shown below, the difference 
is more apparent, where, once again, the first image has alpha = 0.2, and the second one has alpha = 0.9, and the second
jump as been moved to the position (6, 0), landing in (6, 7) as before:

| ![alpha3.png](READMEimages/alpha/alpha3.png) | ![alpha4.png](READMEimages/alpha/alpha4.png) |
|----------------------------------------------|----------------------------------------------|

As we can see in the first image, the agent hasn't really recognized the second jump, because of the slow convergence, 
meanwhile in the second, having faster updates, the agent takes more advantage of the second jump in (6 ,0).

* ### Episode Length:
The **episode length** parameter determines the maximum number of steps allowed in each episode. A longer episode means
the agent is allowed to explore more of the environment, while a shorter episode may lead to premature convergence and 
limit learning in larger grids.

An example, once again, can be seen below, where the first image has the default episode length of 100, and the second 
one has been updated to 300:

| ![episode1.png](READMEimages/episodeLength/length1.png) | ![episode2.png](READMEimages/episodeLength/length2.png) |
|----------------------------------------------|--------------------------------------------------------------|