package demo

import org.apache.kafka.clients.producer.ProducerRecord
import ox.{pipe, sleep}
import ox.flow.Flow
import ox.kafka.ConsumerSettings.AutoOffsetReset
import ox.kafka.{ConsumerSettings, KafkaDrain, KafkaFlow, ProducerSettings, SendPacket}

import scala.concurrent.duration.*

@main def demoKafkaConsumer(): Unit =
  val consumerSettings = ConsumerSettings
    .default("my_group1")
    .bootstrapServers("localhost:9092")
    .autoOffsetReset(AutoOffsetReset.Earliest)
  val producerSettings = ProducerSettings.default.bootstrapServers("localhost:9092")
  val sourceTopic = "test-topic1"
  val destTopic = "test-topic2"

  KafkaFlow
    .subscribe(consumerSettings, sourceTopic)
    .map(in => (in.value ++ in.value, in))
    .tap { in =>
      println(s"Processed ${in._1}")
      sleep(2.seconds)
    }
    .map((value, original) =>
      SendPacket(ProducerRecord[String, String](destTopic, value), original)
    )
    .pipe(KafkaDrain.runPublishAndCommit(producerSettings))

@main def demoKafkaProducer(): Unit =
  val settings = ProducerSettings.default.bootstrapServers("localhost:9092")
  Flow
    .iterate(0)(_ + 1)
    .map(number => s"msg$number")
    .tap { msg =>
      println(s"Sent $msg")
      sleep(1.second)
    }
    .map(msg => ProducerRecord[String, String]("test-topic1", msg))
    .pipe(KafkaDrain.runPublish(settings))
