package controllers

import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import com.typesafe.config.Config
import play.api.Configuration
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
}