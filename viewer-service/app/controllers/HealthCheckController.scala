package controllers


import akka.actor.ActorRef
import akka.pattern.ask
import play.api.Configuration
import play.api.mvc._

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class HealthCheckController @Inject() (@Named("health-check-actor") healthCheckActor: ActorRef,
                                       cc: ControllerComponents,config: Configuration)(implicit ec: ExecutionContext)
  extends BaseController(cc,config) {
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
