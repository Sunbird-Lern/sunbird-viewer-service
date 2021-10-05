package filter

import akka.stream.Materializer
import filter.Attributes.USER_ID
import org.sunbird.auth.verifier.AccessTokenValidator
import play.api.libs.typedmap.TypedKey
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.routing.{HandlerDef, Router}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

object Attributes{
    val USER_ID = TypedKey.apply[String]("userId")
}

class RequestInterceptor @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
    // $COVERAGE-OFF$ Disabling scoverage for Interceptor
    implicit val className = "org.sunbird.viewer.service"

    def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {

        val startTime = System.currentTimeMillis()
        val msgid = UUID.randomUUID().toString
        val userId =AccessTokenValidator.verifyUserToken(request.headers.get("x-authenticated-user-token").get, false)

        val msg = s"${msgid} | Method: ${request.method} | Path: ${request.uri} | Remote Address: ${request.remoteAddress} " +
            s"| Domain=${request.domain} | Params: ${request.rawQueryString} " +
            s"| User-Agent: [${request.headers.get("user-agent").getOrElse("N/A")}]"
        play.Logger.of("accesslog").info(msg)
        next(request.addAttr(USER_ID,userId)).map { result =>
            val endTime = System.currentTimeMillis
            val requestTime = endTime - startTime
            val handlerDef: HandlerDef = request.attrs(Router.Attrs.HandlerDef)
            val apiName = handlerDef.controller
            val queryParamsData = List(request.queryString.map { case (k, v) => k -> v.mkString })
            val paramsData =  Map("status" -> result.header.status, "rid" -> apiName, "title" -> apiName, "duration" -> requestTime, "protocol" -> "", "method" -> request.method,"category" -> "", "size" -> "") :: queryParamsData
                result.withHeaders("Request-Time" -> requestTime.toString)

        }
    }
}