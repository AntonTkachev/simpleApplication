package ru.sbt

import java.io.File

import com.typesafe.config.ConfigFactory
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.compat.Platform
import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._

object Test extends App with ConfigHelper {

  implicit val serializer = new StringSerializer
  implicit val deserializer = new StringDeserializer

  EmbeddedKafka.start()

  val path = "config/consumer.conf"

  Try(Source.fromFile(new File(path))) match {
    case Success(pathToFile) =>

      val file = pathToFile.getLines().mkString(Platform.EOL)
      val root = ConfigFactory.parseString(file).getConfig("consumer")
      val topics = root.getStringList("topics").asScala
      val messages = root.getStringList("messages").asScala
      val bootstrapServer = root.getPropertiesMap("properties")
      val port = bootstrapServer.get("bootstrap.servers") match {
        case bs: String => bs.split(':')(1) //fixme чет не нравится
        case _ => throw new IllegalArgumentException("ERAR")
      }

      implicit val kafkaConfig = EmbeddedKafkaConfig(kafkaPort = port.toInt, zooKeeperPort = 2185)

      topics.foreach(topic => EmbeddedKafka.createCustomTopic(topic))

      topics.foreach { topic =>
        messages.foreach(msg => EmbeddedKafka.publishToKafka(topic, msg)) //TODO в метод
      }

      val kafkaConsumer = new KafkaConsumer[String, String](bootstrapServer)
      kafkaConsumer.subscribe(topics.asJava)
      topics.foreach(kafkaConsumer.partitionsFor)
      val records = kafkaConsumer.poll(5000)
      val recordIter = records.iterator()
      while (recordIter.hasNext) {
        val record = recordIter.next()
        println(record.value() + "!!!")
      }

    case Failure(_) => println("ERROR")
  }

  EmbeddedKafka.stop()
}
