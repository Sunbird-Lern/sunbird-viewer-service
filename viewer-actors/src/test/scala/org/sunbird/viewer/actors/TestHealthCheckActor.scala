package org.sunbird.viewer.actors

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.scalatest.Matchers
import org.sunbird.viewer.spec.BaseSpec

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class TestHealthCheckActor extends BaseSpec  with Matchers with EmbeddedKafka {


    private implicit val system: ActorSystem = ActorSystem("test-actor-system", config)
    val healthRefActor = TestActorRef(new HealthCheckActor())
    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val timeout: Timeout = 20.seconds
    override def beforeAll(): Unit = {
        super.beforeAll()
    }
    override def afterAll(): Unit = {
        super.afterAll()
    }
    "HealthCheckActor" should "test receive function" in {
        healthRefActor.underlyingActor.receive("checkhealth")
    }

    "HealthCheckActor" should "return success response for valid  request" in {
        val response = healthRefActor.underlyingActor.getHealthStatus()
        response.contains("{\"name\":\"viewer.service.health.api\",\"healthy\":\"true\"}") should be (true)
    }

    "HealthCheckActor" should "return success response for service check" in {
        implicit val config = EmbeddedKafkaConfig(kafkaPort = 9092)
        withRunningKafka {
            val response = healthRefActor.underlyingActor.getServiceHealthStatus(List("redis", "cassandra", "kafka"))
            response should be(
                s"""{"name":"viewer.service.health.api","services":[{"name":"redis","healthy":true,"errMsg":null},{"name":"cassandra","healthy":true,"errMsg":null},{"name":"kafka","healthy":true,"errMsg":null}]}""".stripMargin)
        }
    }

}
