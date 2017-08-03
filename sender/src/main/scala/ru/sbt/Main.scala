package ru.sbt

import java.io.File

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.errors.TimeoutException

import scala.collection.JavaConverters._
import scala.compat.Platform
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Main extends StrictLogging with ConfigHelper {

  def main(args: Array[String]): Unit = {

    val path = "config/app.conf"

    Try(Source.fromFile(new File(path))) match {
      case Success(pathToFile) =>

        val file = pathToFile.getLines().mkString(Platform.EOL)
        val root = ConfigFactory.parseString(file).getConfig("producer")
        val topics = root.getStringList("topics")
        val bootstrapServer = root.getPropertiesMap("properties")
        val msgString = Source.fromFile(new File("config/msg.txt")).getLines().mkString(Platform.EOL)

        val kafkaProducer = new KafkaProducer[String, String](bootstrapServer)

//        topics.asScala.foreach(topic =>
         val res =  kafkaProducer.send(new ProducerRecord[String, String](topics.asScala.head, msgString))
//        )
//    , new Callback {
//            override def onCompletion(metadata: RecordMetadata, exception: Exception) : Unit = {
//            Option(exception) match {
//              case Some(ex) =>
//                ex match {
//                  case e: TimeoutException => logger.error(s"Instance kafka not found on bootstrap.servers")
//                  case _ => logger.error(ex.getMessage)
//                }
//                System.exit(1)
//              case _ =>
//                logger.info("Msg sent successfully")
//                System.exit(0)
//            }}
//          })
//        )
      case Failure(_) => logger.error("ERROR")
    }
  }
}
