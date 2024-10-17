package demo

import ox.flow.Flow
import ox.sleep

import scala.concurrent.duration.*
import scala.util.Random

@main def demoZip(): Unit =
  val loopingLetters = Flow.repeat('a' to 'z').mapConcat(identity)
  val numbers = Flow.iterate(0)(_ + 1)
  loopingLetters.zip(numbers).take(100).runForeach(println)

@main def demoMerge(): Unit =
  val f1 = Flow.tick(123.millis, "left")
  val f2 = Flow.tick(312.millis, "right")

  f1.merge(f2).take(100).runForeach(println)

def namesFlow = Flow
  .fromInputStream(this.getClass().getResourceAsStream("/first-names.txt"))
  .linesUtf8
  .map(_.toLowerCase.capitalize)

@main def demoMapPar(): Unit =
  import sttp.client4.quick.*
  namesFlow
    .mapPar(4)(name => println(quickRequest.get(uri"http://localhost:8080/hello?name=$name").send().body))
    .runDrain()

@main def demoControlFlow(): Unit =
  import sttp.client4.quick.*
  namesFlow
    .mapPar(4) { name =>
      if name.endsWith("sław") then println(quickRequest.get(uri"http://localhost:8080/hello?name=${name}a").send().body)
      else println(s"Skipping $name")
    }
    .runDrain()

@main def startHttpServer(): Unit =
  import sttp.tapir.*
  import sttp.tapir.server.netty.sync.NettySyncServer

  val helloWorld = endpoint.get
    .in("hello")
    .in(query[String]("name"))
    .out(stringBody)
    .handleSuccess { name =>
      sleep(Random.nextInt(1000).millis)
      s"Hello, $name!"
    }

  NettySyncServer().addEndpoint(helloWorld).startAndWait()

@main def demoExceptions(): Unit =
  val f1 = Flow
    .iterate(0)(_ + 1)
    .tap(_ => sleep(1.second))
    .map { i =>
      if i > 3 then throw new RuntimeException("Boom!")
      else i
    }

  val f2 = Flow.tick(500.millis, "tick")

  f1.merge(f2).runForeach(println)
