package org.sunbird.viewer.util

import org.joda.time.{DateTime, DateTimeZone}
import org.sunbird.viewer.platform.APIResponseUtil
import org.sunbird.viewer.{Params, Response, ResponseCode}

import java.util.UUID

object ResponseUtil extends APIResponseUtil{
  override def OK(apiId: String, result: Map[String, AnyRef]): Response = {
    Response(apiId, "1.0", df.print(DateTime.now(DateTimeZone.UTC).getMillis), Params(UUID.randomUUID.toString, null, null, "successful", null), ResponseCode.OK.toString, Option(result))
  }

  override def clientErrorResponse(apiId: String, errResponse: Map[String, AnyRef]): Response = {
    Response(apiId, "1.0", df.print(System.currentTimeMillis()),
      Params(UUID.randomUUID().toString, null, ResponseCode.CLIENT_ERROR.toString, "failed", null),
      ResponseCode.CLIENT_ERROR.toString, Some(errResponse))
  }

  override def serverErrorResponse(apiId: String, errResponse: Map[String, AnyRef]): Response = {
    Response(apiId, "1.0", df.print(System.currentTimeMillis()),
      Params(UUID.randomUUID().toString, null, ResponseCode.SERVER_ERROR.toString, "failed", null),
      ResponseCode.SERVER_ERROR.toString, Some(errResponse))
  }
}
