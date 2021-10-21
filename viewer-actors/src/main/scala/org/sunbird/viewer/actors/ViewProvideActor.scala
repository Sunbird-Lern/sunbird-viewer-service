package org.sunbird.viewer.actors

import akka.actor.Actor
import com.datastax.driver.core.Row
import com.typesafe.config.Config
import org.sunbird.cloud.storage.util.JSONUtils
import org.sunbird.viewer._
import org.sunbird.viewer.core.{CassandraUtil, LogUtil, RedisUtil, JSONUtils => Json}
import org.sunbird.viewer.util.{QueryUtil, ResponseUtil}

import javax.inject.Inject
import scala.collection.JavaConversions._
import scala.jdk.CollectionConverters.mapAsScalaMapConverter

class ViewProvideActor @Inject()(config: Config, cassandraUtil: CassandraUtil, redisUtil: RedisUtil) extends Actor {

  val connection = redisUtil.getConnection(config.getInt("redis.viewer.db"))
  val logger = new LogUtil("ViewProvide")

  def receive: Receive = {
    case BaseRequest("read", request) => sender() ! read(request)
    case BaseRequest("readall", request) => sender() ! readAll(request)
  }

  def read(requestBody: String): BaseResponse = {
    val request = Json.deserialize[ViewRequest](requestBody)
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_READ_REQUEST, error)
      case Right(request) =>
        val contentMap = connection.hgetAll(request.toString)
        if (contentMap.isEmpty) {
          val statement = QueryUtil.readViewStmt(Constants.CONTENT_CONSUMPTION_TABLE, request)
          val dbMap = cassandraUtil.find(statement).map(row => {
            val content = Map("status" -> row.getInt("status").toString,
              "progressdetails" -> row.getString("progressdetails"))
            connection.hmset(request.toString, mapAsJavaMap(content))
            getContentMap(request, content)
          })
          ResponseUtil.OK(Constants.VIEW_READ_REQUEST, Json.caseClassToMap(ReadContent(request.userId, dbMap.toList)))
        }
        else {
          ResponseUtil.OK(Constants.VIEW_READ_REQUEST, Json.caseClassToMap(ReadContent(request.userId,
            List(getContentMap(request, contentMap.asScala.toMap[String, Any])))))
        }
    }
  }


  def readAll(requestBody: String): BaseResponse = {
    val request = Json.deserialize[ReadAllRequest](requestBody)
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_CONTEXT_READ_ALL_REQUEST, error)
      case Right(request) =>
        val column = if (!request.contentId.isEmpty) "contentid" else "collectionid"
        val statement = QueryUtil.readAllViewStmt(Constants.CONTENT_CONSUMPTION_TABLE,
          column, request.contentId.getOrElse(request.collectionId.get))
        val dbMap = cassandraUtil.find(statement).map(row => {
          getAllContentMap(row)
        })
        ResponseUtil.OK(Constants.VIEW_CONTEXT_READ_ALL_REQUEST, Json.caseClassToMap(ReadAllContent(request.userId,(dbMap.toList))))
    }
  }

  def getContentMap(request: BaseViewRequest, content: Map[String, Any]): ContentSpec = {
    ContentSpec(request.contentId, request.collectionId.get, request.contextId.get, content.get("status").get.toString,
      JSONUtils.deserialize[Map[String, AnyRef]](content.get("progressdetails").get.toString)
    )
  }

  def getAllContentMap(row: Row): ContentSpec = {
    ContentSpec(row.getString("contentid"), row.getString("collectionid"),
      row.getString("contextid"), row.getInt("status").toString,
      JSONUtils.deserialize[Map[String, AnyRef]](row.getString("progressdetails"))
    )
  }

}
