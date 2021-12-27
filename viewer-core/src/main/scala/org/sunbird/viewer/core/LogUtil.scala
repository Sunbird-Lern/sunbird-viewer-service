package org.sunbird.viewer.core

import org.apache.log4j.LogManager
import org.sunbird.telemetry.{TelemetryGenerator, TelemetryParams}

import scala.collection.JavaConverters.mapAsJavaMap
class LogUtil(name: String) {
  val logger =  LogManager.getLogger(name)
  val context = Map(TelemetryParams.ACTOR.name() -> "org.sunbird.view.service",
    TelemetryParams.ENV.name() -> AppConfig.getString("env"),TelemetryParams.APP_ID.name() -> "sunbird.view.api")

  def info(`type`:String, data:Map[String,AnyRef]): Unit = {
    val logEvent =TelemetryGenerator.log(mapAsJavaMap(context),
      `type`,"info","","",mapAsJavaMap(data))
    logger.info(logEvent)
  }

}
