package controllers


import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import play.api.Configuration
import play.api.mvc._

import javax.inject.{Inject, Named}
import scala.concurrent.duration._
import akka.util.Timeout
import org.sunbird.viewer.service.HealthCheckService

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class HealthCheckController @Inject() (@Named("health-check-actor") healthCheckActor: ActorRef,
                                       cc: ControllerComponents, system: ActorSystem,
                                       configuration: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  implicit val timeout: Timeout =  20 seconds
  def getHealthCheckStatus() = Action.async {

    val result = ask(healthCheckActor, "checkhealth").mapTo[String]
    result.map { x =>
      Ok(x).withHeaders(CONTENT_TYPE -> "application/json");
    }
  }

  def getServiceHealthCheckStatus()  = Action.async {
    val result = ask(healthCheckActor, "checkserviceshealth").mapTo[String]
      result.map { x =>
        Ok(x).withHeaders(CONTENT_TYPE -> "application/json");
      }
  }

}
