package filter

import akka.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.routing.{HandlerDef, Router}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestInterceptor @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
    // $COVERAGE-OFF$ Disabling scoverage for Interceptor
    implicit val className = "org.sunbird.viewing.service"

    def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {

        val startTime = System.currentTimeMillis()
        val msgid = UUID.randomUUID().toString
        request.headers.add(("msgid", msgid))
        val msg = s"${msgid} | Method: ${request.method} | Path: ${request.uri} | Remote Address: ${request.remoteAddress} " +
            s"| Domain=${request.domain} | Params: ${request.rawQueryString} " +
            s"| User-Agent: [${request.headers.get("user-agent").getOrElse("N/A")}]"
        play.Logger.of("accesslog").info(msg)
        next(request).map { result =>
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