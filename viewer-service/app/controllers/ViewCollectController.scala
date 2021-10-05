package controllers

import akka.actor.ActorRef
import akka.pattern.ask
import filter.Attributes.USER_ID
import org.sunbird.viewer.core.JSONUtils
import org.sunbird.viewer.{BaseRequest, BaseResponse, ViewRequestBody}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class ViewCollectController @Inject()(@Named("view-collect-actor") collectActor: ActorRef,
                                      cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext)
  extends BaseController(cc,config) {
  def start() : Action[AnyContent]= Action.async { request: Request[AnyContent] =>
    val body: String = Json.stringify(request.body.asJson.get)
    val requestBody = JSONUtils.deserialize[ViewRequestBody](body).request
      .+(("userId", request.attrs.get(USER_ID).getOrElse(null)))
    val res = ask(collectActor, BaseRequest("start",JSONUtils.serialize(requestBody))).mapTo[BaseResponse]
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }

  def update()  = Action.async {
    val result = ask(collectActor,  "update").mapTo[String]
      result.map { x =>
        Ok(x).withHeaders(CONTENT_TYPE -> "application/json");
      }
  }

}
