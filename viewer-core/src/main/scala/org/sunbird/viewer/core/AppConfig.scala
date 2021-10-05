package org.sunbird.viewer.core

import com.typesafe.config.ConfigFactory

object AppConfig {

  val defaultConf = ConfigFactory.load()
  val envConf = ConfigFactory.systemEnvironment()
  val conf = envConf.withFallback(defaultConf)

  def getString(key: String): String = {
    conf.getString(key)
  }

  def getInt(key: String): Int = {
    conf.getInt(key)
  }

  def getDouble(key: String): Double = {
    conf.getDouble(key)
  }

}
