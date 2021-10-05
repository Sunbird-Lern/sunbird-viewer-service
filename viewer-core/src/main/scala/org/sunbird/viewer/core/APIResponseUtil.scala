package org.sunbird.viewer.core

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

trait APIResponse
trait APIResponseUtil {

  @transient val df: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").withZoneUTC()

  def OK(apiId: String, result: Map[String, AnyRef]) : APIResponse

  def clientErrorResponse(apiId: String,errResponse: Map[String, AnyRef]) : APIResponse

  def serverErrorResponse(apiId: String,errResponse: Map[String, AnyRef]) : APIResponse


}
