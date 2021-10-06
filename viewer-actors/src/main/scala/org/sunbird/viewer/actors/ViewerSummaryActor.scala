package org.sunbird.viewer.actors

import akka.actor.Actor
import com.datastax.driver.core.TypeTokens
import org.sunbird.viewer.core.{AppConfig, CassandraUtil, HTTPResponse, HttpUtil, JSONUtils, ServerException}
import org.sunbird.viewer.util.{QueryUtil, ResponseUtil}
import org.sunbird.viewer._

import javax.inject.Inject
import scala.collection.JavaConverters._

class ViewerSummaryActor @Inject() extends  Actor{
 val cassandraUtil: CassandraUtil = new CassandraUtil
  implicit val httpUtil: HttpUtil = new HttpUtil
  val baseUrl = {if(AppConfig.conf.hasPath("service.search.basePath"))AppConfig.getString("service.search.basePath") else "https://dev.sunbirded.org"}
  val searchUrl = {if(AppConfig.conf.hasPath("service.search.basePath"))AppConfig.getString("service.search.basePath") else "/api/content/v1/search"}

  override def receive: Receive = {
    case BaseRequest("summary-list", userId) => sender() ! summaryList(userId)
    case BaseRequest("summary-read", request) => sender() ! summaryRead(request)  
  }

  def summaryList(userId: String): Response = {
    // cache data
    
    try {
      // read all enrolments with status = 2
      val enrolments: Map[String, EnrolmentData] = readEnrolments(userId)
      // read all user-activity-agg
      val activityData = readUserActivity(userId, enrolments.keySet.toList)
      // get all content metadata from search
      val collectionMetadata = searchMetadata(enrolments.keySet.toList)
      // merge all and send the response
      prepareResponse(Constants.VIEW_SUMMARY_LIST_API_ID, enrolments, activityData, collectionMetadata)
    } catch {
      case e: Exception => ResponseUtil
        .serverErrorResponse(Constants.VIEW_SUMMARY_LIST_API_ID, Map("message" -> e.getMessage))
    }
  }

  def summaryRead(request: String): Response = {
    val req = JSONUtils.deserialize[ViewerSummaryRequest](request)
    // cache data

    try {
      // read all enrolments with status = 2
      val enrolments: Map[String, EnrolmentData] = readEnrolments(req.userId, req.collectionId.get, req.contextId.get)
      // read all user-activity-agg
      val activityData = readUserActivity(req.userId, enrolments.keySet.toList)
      // get all content metadata from search
      val collectionMetadata = searchMetadata(enrolments.keySet.toList)
      // merge all and send the response
      prepareResponse(Constants.VIEW_SUMMARY_READ_API_ID, enrolments, activityData, collectionMetadata)
    } catch {
      case e: Exception => ResponseUtil
        .serverErrorResponse(Constants.VIEW_SUMMARY_READ_API_ID, Map("message" -> e.getMessage))
    }
  }
  
  def readEnrolments(userId: String, collectionId: String = null, batchId: String = null):Map[String, EnrolmentData] = {
    val query = QueryUtil.getEnrolments(ViewerSummaryRequest(userId, Option(collectionId), Option(batchId)))
    val rows = cassandraUtil.find(query)
    if(null != rows && !rows.asScala.isEmpty) {
      rows.asScala.map(row => row.getString("courseid") -> EnrolmentData(row.getString("userid"), row.getString("courseid"),
        row.getString("batchid"), row.getDate("enrolled_on").toString, row.getBool("active"),
        row.getList("issued_certificates", TypeTokens.mapOf(classOf[String], classOf[String])),
        row.getDate("completed_on").toString, row.getInt("progress"), row.getInt("status"))).toMap
    } else {
      Map()
    }
  }

  def readUserActivity(userId: String, collectionIds: List[String], contextIds: List[String] = List()) = {
    val query = QueryUtil.getUserActivities(userId, collectionIds)
    val rows = cassandraUtil.find(query)
    //TODO: Need to verify if context filtering is required or not
    //rows.asScala.filter(row => contextIds.contains(row.getString("context_id").replaceAll("cb:","")))
    rows.asScala.map(row => {
        val contentStatus = row.getMap[String, Int]("content_status", classOf[String], classOf[Int]).asScala.toMap
        row.getString("activity_id") -> UserActivityData(row.getString("user_id"), row.getString("activity_id"),row.getString("context_id").replaceAll("cb:",""),
          contentStatus , getAssessStatus(row.getMap[String, Int]("agg", classOf[String], classOf[Int]), contentStatus.keys.toList))
      }).toMap
  }

  def getAssessStatus(aggs: java.util.Map[String, Int], leafNodes: List[String]): Map[String, Map[String, AnyRef]] = {
    val filteredIds = leafNodes.filter(id => 0 != aggs.getOrDefault("score:" + id, 0))
    if(!filteredIds.isEmpty) {
      filteredIds.map(id => id -> Map("score" -> aggs.getOrDefault("score:" + id, 0).asInstanceOf[AnyRef], "maxScore" -> aggs.getOrDefault("max_score:" + id, 0).asInstanceOf[AnyRef]).toMap).toMap
    } else Map() 
  }
  
  def searchMetadata(collectionIds: List[String]) = {
    val request = s"""{"request":{"filters":{"identifier": ${collectionIds.mkString("[\"", "\",\"", "\"]")},"status":"Live","mimeType":"application/vnd.ekstep.content-collection","trackable.enabled":"Yes"},"fields":[]}}"""
    val httpResponse: HTTPResponse = httpUtil.post(baseUrl + searchUrl, request)
    if(200 == httpResponse.status) {
      val response = JSONUtils.deserialize[Response](httpResponse.body)
      response.result.getOrElse("content", List()).asInstanceOf[List[Map[String, AnyRef]]].map(content => (content.getOrElse("identifier","").asInstanceOf[String] -> content)).toMap
    } else {
      throw ServerException(s"Error while searching collection metadata : ${httpResponse.status} : ${httpResponse.body}")
    }
  }

  def prepareResponse(apiId:String, enrolments: Map[String, EnrolmentData], activityData: Map[String, UserActivityData],
                      collectionMetadata: Map[String, Map[String, AnyRef]]) = {
    val summary = enrolments.map(enrolment => {
      val enrolmentData = enrolment._2
      val userActivityData = activityData.get(enrolment._1).get
      val collectionData = collectionMetadata.get(enrolment._1).get
      
      Summary(enrolmentData.userId, enrolmentData.collectionId, enrolmentData.contextId, enrolmentData.enrolledDate, enrolmentData.active,
        userActivityData.contentStatus, userActivityData.assessmentStatus, collectionData, enrolmentData.issuedCertificates, enrolmentData.completedOn,
        enrolmentData.progress, enrolmentData.status)
    }).toList
    
    ResponseUtil.OK(apiId, Map("summary" -> summary))
  }
  
  
}
