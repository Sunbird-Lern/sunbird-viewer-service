package controllers

import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import com.typesafe.config.Config
import filter.Attributes.USER_ID
import org.sunbird.viewer.ViewRequestBody
import org.sunbird.viewer.core.JSONUtils
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.duration.DurationInt


class BaseController(cc: ControllerComponents, configuration: Configuration) extends AbstractController(cc) {

  implicit val className = "controllers.BaseController"

  implicit lazy val config: Config = configuration.underlying

  implicit val timeout: Timeout = 20 seconds

  def result(code: String, res: String): Result = {
    val resultObj = code match {
      case "OK" =>
        Ok(res)
      case "CLIENT_ERROR" =>
        BadRequest(res)
      case "SERVER_ERROR" =>
        InternalServerError(res)
    }
    resultObj.withHeaders(CONTENT_TYPE -> "application/json")
  }

  def updateUserId(request:Request[AnyContent]): String = {
    val requestBody =Json.stringify(request.body.asJson.get)
    JSONUtils.serialize(JSONUtils.deserialize[ViewRequestBody](requestBody).request
      .+(("userId", request.attrs.get(USER_ID).getOrElse(null))))
  }
}