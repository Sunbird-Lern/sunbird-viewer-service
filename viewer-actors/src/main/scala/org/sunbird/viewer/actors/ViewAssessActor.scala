package org.sunbird.viewer.actors

import akka.actor.Actor
import com.datastax.driver.core.UDTValue
import com.google.gson.Gson
import com.typesafe.config.Config
import org.sunbird.cloud.storage.util.JSONUtils
import org.sunbird.viewer._
import org.sunbird.viewer.core.{CassandraUtil, KafkaUtil, LogUtil, RedisUtil, JSONUtils => Json}
import org.sunbird.viewer.util.{QueryUtil, ResponseUtil}

import java.math.BigDecimal
import java.sql.Timestamp
import java.util
import javax.inject.Inject
import scala.collection.JavaConverters._


class ViewAssessActor @Inject()(config: Config, cassandraUtil: CassandraUtil, redisUtil: RedisUtil, kafkaUtil: KafkaUtil) extends Actor {

  val relationCache = redisUtil.getConnection(config.getInt("redis.relation.db"))
  val viewerCache = redisUtil.getConnection(config.getInt("redis.viewer.db"))
  val questionType = cassandraUtil.getUDTType(config.getString("lms-cassandra.keyspace"), config.getString("lms-cassandra.questionudttype"))
  val logger = new LogUtil("ViewAssess")

  def receive: Receive = {
    case BaseRequest("save", request) => sender() ! save(request)
    case BaseRequest("submit", request) => sender() ! submit(request)
  }

  def save(requestBody: String):BaseResponse = {
    val request = Json.deserialize[ViewAssessRequest](requestBody)
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_ASSESS_SUBMIT_REQUEST, error)
        val result =request.assessments.map(assess =>{
          assess.validateRequest match {
            case Left(error) => (assess.contentId,error)
            case Right(assess) =>
              if(isValidContent(assess.collectionId.get,assess.contentId)) {
                val statement = QueryUtil.startViewStmt(Constants.CONTENT_CONSUMPTION_TABLE, assess)
                cassandraUtil.executeStmt(statement,getQuestionList(assess.events).map(question => getQuestion(question.edata,assess.assessmentTs)))
                (assess.contentId,"Saved Assessment Successfully")
              }
              else {
                kafkaUtil.send(JSONUtils.serialize(assess),config.getString("kafka_assessment_failed_topic"))
                (assess.contentId , "Invalid Request")
              }
          }
        })
        ResponseUtil.OK(Constants.VIEW_ASSESS_SUBMIT_REQUEST, result.groupBy(_._1).mapValues(f => f.map(z => z._2.toString)))
    }
  }

  def submit(requestBody: String): BaseResponse = {
    val request = Json.deserialize[ViewAssessRequest](requestBody)
    request.validateRequest match {
      case Left(error) => ResponseUtil.clientErrorResponse(Constants.VIEW_ASSESS_SUBMIT_REQUEST, error)
      case Right(request) =>
        val result =request.assessments.map(assess =>{
           assess.validateRequest match {
             case Left(error) => (assess.contentId,error)
             case Right(assess) =>
               if(isValidContent(assess.collectionId.get,assess.contentId)) {
                 val events = getUniqueQuestions(assess.events)
                 updateScore(assess.toString,events)
                 kafkaUtil.send(JSONUtils.serialize(assess),config.getString("kafka_assessment_batch_topic"))
                 (assess.contentId,"Score Captured Successfully")
               }
               else {
                 kafkaUtil.send(JSONUtils.serialize(assess),config.getString("kafka_assessment_failed_topic"))
               (assess.contentId , "Invalid Request")
               }
           }
        })
        ResponseUtil.OK(Constants.VIEW_ASSESS_SUBMIT_REQUEST, result.groupBy(_._1).mapValues(f => f.map(z => z._2.toString)))
    }
  }

  def getUniqueQuestions(events:List[Map[String,AnyRef]]) : List[AssessEvent] ={
    getQuestionList(events).sortWith(_.ets > _.ets).groupBy(_.edata.item.id).map(_._2.head).toList
  }


  def isValidContent(courseId: String, contentId: String): Boolean = {
    val leafNodes = redisUtil.getKeyMembers(relationCache,s"$courseId:$courseId:leafnodes")
    if (!leafNodes.isEmpty) {
      leafNodes.contains(contentId)
    } else {
      true
    }
  }

  def updateScore(key:String,events: List[AssessEvent]): String = {
    var totalScore = 0.0
    var totalMaxScore = 0.0
    events.map(event => {
      totalScore = totalScore + event.edata.score
      totalMaxScore = totalMaxScore + event.edata.item.maxscore
    })
    viewerCache.hmset(key, mapAsJavaMap(Map("score" -> totalScore.toString, "maxScore" -> totalMaxScore.toString )))
  }

  def getQuestionList(events: List[Map[String,AnyRef]]) ={
    events.map(event =>{
      AssessEvent(event("ets").asInstanceOf[Long], JSONUtils.deserialize[QuestionData](JSONUtils.serialize(event("edata"))))
    })
  }

  def getListValues(values: List[Map[String,Any]]): List[util.Map[String, Any]] = {
    values.map { res =>
      res.map {
        case (key, value) => key -> (if (null != value && !value.isInstanceOf[String]) new Gson().toJson(value) else value)
      }.asJava
    }
  }

  def getQuestion(questionData: QuestionData, assessTs: Long): UDTValue = {
    questionType.newValue().setString("id", questionData.item.id).setDouble("max_score", questionData.item.maxscore)
      .setDouble("score", questionData.score)
      .setString("type", questionData.item.`type`)
      .setString("title", questionData.item.title)
      .setList("resvalues", getListValues(questionData.resvalues).asJava).setList("params", getListValues(questionData.item.params).asJava)
      .setString("description", questionData.item.desc)
      .setDecimal("duration", BigDecimal.valueOf(questionData.duration)).setTimestamp("assess_ts", new Timestamp(assessTs))
  }
}
