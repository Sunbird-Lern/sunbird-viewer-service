package org.sunbird.viewer.service

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class TestHealthCheckService extends FlatSpec with Matchers {


    implicit val config = ConfigFactory.load()
    private implicit val system: ActorSystem = ActorSystem("test-actor-system", config)
    val healthRefActor = TestActorRef(new HealthCheckService())
    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val timeout: Timeout = 20.seconds

    "HealthCheckService" should "test receive function" in {
        healthRefActor.underlyingActor.receive("checkhealth")
    }

    "HealthCheckService" should "return success response for valid  request" in {
        val response = healthRefActor.underlyingActor.getHealthStatus()
        response.contains("{\"name\":\"viewer.service.health.api\",\"healthy\":\"true\"}") should be (true)
    }

}
