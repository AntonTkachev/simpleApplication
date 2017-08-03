package ru.sbt

import java.io.File

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.compat.Platform
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Main extends StrictLogging with ConfigHelper {
  def main(args: Array[String]): Unit = {

    val path = "config/app.conf"

    Try(Source.fromFile(new File(path))) match {
      case Success(pathToFile) =>

        val file = pathToFile.getLines().mkString(Platform.EOL)
        val root = ConfigFactory.parseString(file).getConfig("consumer")
        val topics = root.getStringList("topics")
        val bootstrapServer = root.getPropertiesMap("properties")

        val kafkaConsumer = new KafkaConsumer[String, String](bootstrapServer)
        kafkaConsumer.subscribe(topics)
        val iterator = kafkaConsumer.poll(100).iterator()
        while (iterator.hasNext) {
          val record = iterator.next()
          println(record)
        }

      case Failure(_) => logger.error("ERROR")
    }
  }
}
