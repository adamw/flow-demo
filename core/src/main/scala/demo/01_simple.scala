package demo

import ox.pipe
import ox.flow.Flow

@main def simple1(): Unit =
  Flow
    .fromValues(11, 24, 51, 76, 78, 9, 1, 44)
    .map(_ + 3)
    .filter(_ % 2 == 0)
    .intersperse(5)
    .mapStateful(() => 0) { (state, value) =>
      val newState = state + value
      (newState, newState)
    }
    .runToList()
    .pipe(println)

@main def simple2(): Unit =
  Flow
    .iterate(0)(_ + 1)
    .filter(_ % 3 == 0)
    .take(5)
    .runForeach(println)
