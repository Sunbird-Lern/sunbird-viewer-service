package org.sunbird.viewer.util

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, DateTimeZone}
import org.sunbird.viewer.{Params, Response, ResponseCode}

import java.util.UUID

object ResponseUtil {

  @transient val df: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").withZoneUTC();
  def OK(apiId: String, result: Map[String, AnyRef]): Response = {
    Response(apiId, "1.0", df.print(DateTime.now(DateTimeZone.UTC).getMillis), Params(UUID.randomUUID().toString(), null, null, "successful", null), ResponseCode.OK.toString(), Option(result));
  }


  def errorResponse(apiId: String, errResponse: Map[String, String], responseCode: String): Response = {
    Response(apiId, "1.0", df.print(System.currentTimeMillis()),
      Params(UUID.randomUUID().toString, null, responseCode, "failed", null),
      responseCode, Some(errResponse))
  }

}
