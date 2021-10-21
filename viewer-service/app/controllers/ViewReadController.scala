package controllers

import akka.actor.ActorRef
import akka.pattern.ask
import org.sunbird.viewer.core.JSONUtils
import org.sunbird.viewer.{BaseRequest, BaseResponse}
import play.api.Configuration
import play.api.mvc._

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class ViewReadController @Inject()(@Named("view-provide-actor") provideActor: ActorRef,
                                   cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext)
  extends BaseController(cc,config) {
  def read() : Action[AnyContent]= Action.async { request: Request[AnyContent] =>
    val requestBody =updateUserId(request)
    val res = request.getQueryString("context").map({ context =>
      ask(provideActor, BaseRequest("readall",requestBody)).mapTo[BaseResponse]
    }).getOrElse({
      ask(provideActor, BaseRequest("read",requestBody)).mapTo[BaseResponse]
    })
    res.map { x =>
      result(x.responseCode,JSONUtils.serialize(x))
    }
  }

}
