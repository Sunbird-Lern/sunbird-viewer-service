package org.sunbird.viewer.actors

import akka.actor.Actor
import org.sunbird.viewer._
import org.sunbird.viewer.core.{CassandraUtil, JSONUtils}
import org.sunbird.viewer.util.{QueryUtil, ResponseUtil}

import javax.inject.Inject



class ViewCollectActor @Inject()(cassandraUtil: CassandraUtil) extends Actor {
  val convertInt = (text:String) => Integer.valueOf(text)

  def receive: Receive = {
    case BaseRequest("start", request) => sender() ! start(request)
    case BaseRequest("update", request) => sender() ! update(request)
  }

  def start(requestBody: String): BaseResponse = {
    val request = JSONUtils.deserialize[StartRequest](requestBody)
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_START_REQUEST, error)
      case Right(request) =>
        val statement = QueryUtil.getInsertViewStartStatement(Constants.CONTENT_CONSUMPTION_TABLE,request)
        cassandraUtil.executeStatement(statement,List(convertInt(StatusCode.START.toString),
          convertInt(ProgressCode.START.toString)))
        val response = JSONUtils.deserialize[Map[String, AnyRef]](s"""{"${request.contentId}":"Progress started"}""")
        ResponseUtil.OK(Constants.VIEW_START_REQUEST, response)
    }
  }

  def update(request: String): String = {
    val result = Map(
      "name" -> "viewer.service.health.api",
      "services" -> request)
    JSONUtils.serialize(result)
  }
}

