package bootstrap.config

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by sbt-kanishev-aa on 05.04.2017.
  */
object ConfigHelper {

  implicit class RichConfig(val config: Config) {
    def getOptional[T](getter: Config => T): Option[T] = {
      Try(getter.apply(config)).map(Option(_)).getOrElse(None)
    }

    def getPropertiesMap(mapConfigPath: String) = {
      config.getObjectList(mapConfigPath).asScala.flatMap { item =>
        val entrySet = item.entrySet().asScala
        for (entry <- entrySet)
          yield {
            val value: AnyRef = entry.getValue.unwrapped()
            entry.getKey -> s"$value"
          }
      }.toMap
    }

    def apply(config: Config): RichConfig = {
      new RichConfig(config)
    }

    def toRichConfig(config: Config): RichConfig = RichConfig(config)
  }

}
