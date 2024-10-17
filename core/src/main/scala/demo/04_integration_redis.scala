package demo

import io.lettuce.core.{Consumer, RedisBusyException, RedisClient, XReadArgs}
import ox.flow.Flow
import ox.sleep

import scala.jdk.CollectionConverters.*
import scala.concurrent.duration.*

@main def demoRedisRead(): Unit =
  Flow
    .usingEmit[Map[String, String]] { emit =>
      val commands = RedisClient.create("redis://localhost:6379").connect.sync

      try commands.xgroupCreate(XReadArgs.StreamOffset.from("test-stream1", "0-0"), "group1")
      catch case e: RedisBusyException => ()

      while (true) {
        val messages = commands.xreadgroup(Consumer.from("group1", "consumer1"), XReadArgs.StreamOffset.lastConsumed("test-stream1"))
        if (!messages.isEmpty) {
          for (message <- messages.asScala) {
            emit(message.getBody.asScala.toMap)
            commands.xack("test-stream1", "group1", message.getId)
            sleep(2.seconds)
          }
        }
      }
    }
    .tap { msg =>
      val key = msg("key")
      println(s"Processing message with key: $key ...")
    }
    .runDrain()

@main def demoRedisWrite(): Unit =
  val commands = RedisClient.create("redis://localhost:6379").connect.sync

  var counter = 0
  while (true) {
    val msgId = commands.xadd("test-stream1", Map("key" -> s"value$counter").asJava)
    counter += 1
    println(s"Sent: $msgId")
    sleep(1.second)
  }
