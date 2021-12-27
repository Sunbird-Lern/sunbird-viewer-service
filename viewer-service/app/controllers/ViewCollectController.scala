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
    val requestBody =updateUserId(request)
    val res = ask(collectActor, BaseRequest("start",requestBody)).mapTo[BaseResponse]
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }

  def update()  = Action.async { request : Request[AnyContent] =>
    val res = ask(collectActor, BaseRequest("update",updateUserId(request))).mapTo[BaseResponse]
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }

  def end()  = Action.async { request : Request[AnyContent] =>
    val res = ask(collectActor, BaseRequest("end",updateUserId(request))).mapTo[BaseResponse]
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }




}
