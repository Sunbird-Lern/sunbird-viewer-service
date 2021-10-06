package controllers

import akka.actor.ActorRef
import akka.pattern.ask
import org.sunbird.viewer.core.JSONUtils
import org.sunbird.viewer.{BaseRequest, Response}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class StartUpdateController @Inject()(@Named("view-start-update-actor") startUpdateActor: ActorRef,
                                      cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext)
  extends BaseController(cc,config) {
  def start() : Action[AnyContent]= Action.async { request: Request[AnyContent] =>
    val body: String = Json.stringify(request.body.asJson.get)
    val res = ask(startUpdateActor, BaseRequest("start",body)).mapTo[Response]
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }

  def update()  = Action.async {
    val result = ask(startUpdateActor, "update").mapTo[String]
      result.map { x =>
        Ok(x).withHeaders(CONTENT_TYPE -> "application/json");
      }
  }

}
