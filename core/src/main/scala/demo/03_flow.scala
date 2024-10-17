package demo

object Internals:
  class Flow[+T](val last: FlowStage[T])

  trait FlowStage[+T]:
    def run(emit: FlowEmit[T]): Unit

  trait FlowEmit[-T]:
    def apply(t: T): Unit

  /*

  Signal completion: when `run` completes

  Signal error: throw an exception

   */
