package org.sunbird.viewer.actors

import akka.actor.Actor
import com.typesafe.config.Config
import org.sunbird.viewer._
import org.sunbird.viewer.core.{AppConfig, CassandraUtil, JSONUtils, LogUtil, RedisUtil}
import org.sunbird.viewer.util.{QueryUtil, ResponseUtil}

import javax.inject.Inject
import scala.collection.JavaConverters.mapAsJavaMap

class ViewCollectActor @Inject()(config:Config,cassandraUtil: CassandraUtil, redisUtil: RedisUtil) extends Actor {

  val convertInt: String => Integer = (text: String) => Integer.valueOf(text)
  val connection = redisUtil.getConnection(config.getInt("redis.viewer.db"))
  val logger = new LogUtil("ViewCollect")

  def receive: Receive = {
    case BaseRequest("start", request) => sender() ! start(request)
    case BaseRequest("update", request) => sender() ! update(request)
    case BaseRequest("end", request) => sender() ! end(request)
  }

  def start(requestBody: String): BaseResponse = {
    val request = JSONUtils.deserialize[ViewRequest](requestBody)
    val status = StatusCode.START.toString
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_START_REQUEST, error)
      case Right(request) =>
        val statement = QueryUtil.startViewStmt(Constants.CONTENT_CONSUMPTION_TABLE, request)
        if (cassandraUtil.executeStmt(statement, List(convertInt(status)))) {
          connection.hset(request.toString, "status", status)
          logger.info("start", Map("context" -> request.toString, "status" -> status))
        }
        val response = JSONUtils.deserialize[Map[String, AnyRef]](s"""{"${request.contentId}":"Progress started"}""")
        ResponseUtil.OK(Constants.VIEW_START_REQUEST, response)
    }
  }

  def update(requestBody: String): BaseResponse = {
    val request = JSONUtils.deserialize[UpdateRequest](requestBody)
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_UPDATE_REQUEST, error)
      case Right(request) =>
        val progress = JSONUtils.serialize(request.progressdetails)
        val statement = QueryUtil.updateViewStmt(Constants.CONTENT_CONSUMPTION_TABLE, request)
        val dbMap = Map( "progressdetails" -> progress)
        if (cassandraUtil.executeStmt(statement, List(progress))) {
          connection.hmset(request.toString, mapAsJavaMap(dbMap))
          logger.info("update", Map("context" -> request.toString)++ dbMap)
        }
        val response = JSONUtils.deserialize[Map[String, AnyRef]](s"""{"${request.contentId}":"Progress Updated"}""")

        ResponseUtil.OK(Constants.VIEW_END_REQUEST, response)
    }
  }

  def end(requestBody: String): BaseResponse = {
    val request = JSONUtils.deserialize[EndRequest](requestBody)
    val status = StatusCode.END.toString
    var dbMap = Map("status" -> status)
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_END_REQUEST, error)
      case Right(request) =>
        var stmt =QueryUtil.endViewStmt(Constants.CONTENT_CONSUMPTION_TABLE, request,false)
        val dbList = request.progressdetails.map{
          progress => {
            val progressdetails = JSONUtils.serialize(progress)
            stmt = QueryUtil.endViewStmt(Constants.CONTENT_CONSUMPTION_TABLE, request,true)
            dbMap = dbMap++ Map("progressdetails" -> progressdetails)
            List(convertInt(status), progressdetails)
          }
        }.getOrElse(List(convertInt(status)))
        if (cassandraUtil.executeStmt(stmt, dbList )) {
          connection.hmset(request.toString, mapAsJavaMap(dbMap))
          logger.info("end", Map("context" -> request.toString)++dbMap)
        }
        val response = JSONUtils.deserialize[Map[String, AnyRef]](s"""{"${request.contentId}":"Progress Ended"}""")
        ResponseUtil.OK(Constants.VIEW_END_REQUEST, response)
    }
  }

}
