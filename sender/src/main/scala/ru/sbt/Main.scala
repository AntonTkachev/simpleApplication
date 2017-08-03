package ru.sbt

import java.io.File

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.compat.Platform
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Main extends StrictLogging with ConfigHelper {
  def main(args: Array[String]): Unit = {

    val path = "config/app.conf"

    implicit val serializer = new StringSerializer
    implicit val deserializer = new StringDeserializer
    implicit val kafkaConfig = EmbeddedKafkaConfig(kafkaPort = 9096, zooKeeperPort = 2183)

    Try(Source.fromFile(new File(path))) match {
      case Success(pathToFile) =>

        val file = pathToFile.getLines().mkString(Platform.EOL)
        val root = ConfigFactory.parseString(file).getConfig("consumer")
        val topics = root.getStringList("topics")
        val bootstrapServer = root.getPropertiesMap("properties")
//        val msgString = Source.fromFile("").getLines().mkString(Platform.EOL)
        EmbeddedKafka.start()
        topics.forEach { topic => EmbeddedKafka.createCustomTopic(topic/*, bootstrapServer*/) }
        topics.forEach { topic => EmbeddedKafka.publishToKafka(topic, s"$topic msg" /*msgString*/) }
        topics.forEach { topic =>
          val res = EmbeddedKafka.consumeFirstMessageFrom(topic, autoCommit = true)
          println(res)
        }
        EmbeddedKafka.stop()

      case Failure(_) => logger.error("ERROR")
    }
  }
}
