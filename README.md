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
for performance analysis and reliability studies.


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