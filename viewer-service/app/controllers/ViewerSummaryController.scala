package controllers

import akka.actor.ActorRef
import akka.pattern.ask
import org.sunbird.viewer.{BaseRequest, Response}
import org.sunbird.viewer.core.JSONUtils
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request}

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext

class ViewerSummaryController @Inject() (@Named("view-summary-actor") summaryActor: ActorRef,
                                         cc: ControllerComponents,config: Configuration)(implicit ec: ExecutionContext)
  extends BaseController(cc,config) {

  def summaryList(userId: String) : Action[AnyContent]= Action.async {
    val res = ask(summaryActor, BaseRequest("summary-list",userId)).mapTo[Response]
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }

  def summaryRead() : Action[AnyContent]= Action.async { request: Request[AnyContent] =>
    val body: String = Json.stringify(request.body.asJson.get)
    val res = ask(summaryActor, BaseRequest("summary-read",body)).mapTo[Response]
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }
}
