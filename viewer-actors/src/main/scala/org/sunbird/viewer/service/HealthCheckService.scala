package org.sunbird.viewer.service

import akka.actor.Actor
import org.sunbird.viewer.platform.{CassandraUtil, JSONUtils, KafkaUtil, RedisUtil}

import javax.inject.Inject

case class ServiceHealth(name: String, healthy: Boolean, errMsg: Option[String] = None)
class HealthCheckService @Inject() extends  Actor {

  def receive: Receive = {
    case "checkhealth" => sender() ! getHealthStatus
    case "checkserviceshealth" => sender() ! getServiceHealthStatus(List("redis","cassandra","kafka"))
  }


  def getHealthStatus(): String = {

    val result = Map(
      "name" -> "viewer.service.health.api",
      "healthy" -> "true")
    JSONUtils.serialize(result)
  }


  def getServiceHealthStatus(services: List[String]): String = {
    val status = services.map(service => {
      try {
        service match {
          case "redis" => ServiceHealth(service, new RedisUtil().checkConnection)
          case "cassandra" => ServiceHealth(service, new CassandraUtil().checkConnection())
          case "kafka" => ServiceHealth(service, new KafkaUtil().checKConnection())
        }
      }
      catch {
        case ex: Exception =>
          ServiceHealth(service, false, Some(ex.getMessage))
      }
    })
   val result= Map(
      "name" -> "viewer.service.health.api",
       "services" -> status)
    JSONUtils.serialize(result)
  }
}

