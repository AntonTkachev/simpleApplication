package ru.sbt

import com.typesafe.config.Config

import scala.collection.JavaConverters._

trait ConfigHelper {

  implicit class RichConfig(val config: Config) {

    def getPropertiesMap(mapConfigPath: String) = {
      config.getObjectList(mapConfigPath).asScala.flatMap { item =>
        val entrySet = item.entrySet().asScala
        for (entry <- entrySet)
          yield {
            val value: AnyRef = entry.getValue.unwrapped()
            entry.getKey -> value
          }
      }.toMap.asJava
    }

/*    def apply(config: Config): RichConfig = {
      new RichConfig(config)
    }

    def toRichConfig(config: Config): RichConfig = RichConfig(config)*/
  }

}
