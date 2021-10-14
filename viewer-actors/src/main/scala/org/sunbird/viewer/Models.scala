package org.sunbird.viewer

import org.sunbird.viewer.core.APIResponse

import java.util.UUID

object Models {
}

object ResponseCode extends Enumeration {
  type Code = Value
  val OK, CLIENT_ERROR, SERVER_ERROR = Value
}
//type StatusCode = Value
object StatusCode extends Enumeration {
  type status = Int
  val START,UPDATE = Value("1")
  val END = Value("2")
}
object ProgressCode extends Enumeration {
  type status = Int
  val START= Value("1")
  val END = Value("100")
}


object Constants{
  val VIEW_START_REQUEST = "api.view.start"
  val VIEW_END_REQUEST = "api.view.end"
  val VIEW_UPDATE_REQUEST = "api.view.update"
  val VIEW_READ_REQUEST = "api.view.read"
  val VIEW_SUMMARY_LIST_API_ID = "api.summary.list"
  val VIEW_SUMMARY_READ_API_ID = "api.summary.read"
  val SUNBIRD_COURSES_KEYSPACE= "sunbird_courses"
  val CONTENT_CONSUMPTION_TABLE="user_content_consumption_new"
  val USER_ENROLMENTS_TABLE="user_enrolments"
  val USER_ACTIVITY_TABLE="user_activity_agg"
  val CONTENT_START_STATUS = 1
  val CONTENT_END_ = 1
}
// Common Class

case class Params(resmsgid: String, msgid: String, err: String, status: String, errmsg: Map[String,String], client_key: Option[String] = None)
case class Response(id: String, ver: String, ts: String, params: Params, responseCode: String, result: Option[Map[String, AnyRef]]) extends APIResponse


case class BaseRequest(`type`:String,request: String)

trait BaseViewRequest {
  def userId: String
  def contentId : String
  def collectionId: Option[String]
  def contextId: Option[String]
}

case class ViewRequestBody(id: String, ver: String, ts: String, request: Map[String,AnyRef], params: Option[Params])


case class StartRequest(userId: String, contentId:String,collectionId :Option[String],contextId:Option[String])
  extends  BaseViewRequest {
  def validateRequest: Either[Map[String,AnyRef],StartRequest] ={
    if(null == userId || userId.isEmpty)
      Left(Map("request.userId" -> "cannot be empty"))
    else  if(null == contentId || contentId.isEmpty)
      Left(Map("request.contentId" -> "cannot be empty"))
    else
      Right(StartRequest(userId,contentId,Some(collectionId.getOrElse(contentId)),
        Some(contextId.getOrElse(collectionId.getOrElse(contentId)
        ))))
  }
}

case class ViewUpdateRequest(userId: String, contentId:String, batchId:String,collectionId :String,progress: Int)

case class ViewEndRequest(userId: String, contentId:String, batchId:String,collectionId :String,
                          assessments: List[Map[String,AnyRef]])


case class ContentEndEvent(eid: String = "BE_JOB_REQUEST", ets: Long = System.currentTimeMillis(),
                           mid: String = UUID.randomUUID.toString, actor: TypeId, context: Context, `object`: TypeId,
                           edata:  EData, action: String, iteration: Int,
                           batchId: String,userId: String, courseId: String)
case class TypeId(`type`: String, id: String)
case class Context(pdata: PData)
case class PData(ver: String , id : String)
case class EData(contents: List[Content])
case class Content(contentId: String, status:Int)

case class ViewerSummaryRequest(userId: String, collectionId: Option[String], contextId: Option[String])

case class Summary(userId: String, collectionId: String, contextId: String, enrolledDate: Long, active: Boolean,
                   contentStatus: Map[String, Integer], assessmentStatus: Map[String, Map[String, AnyRef]],
                   collection: Map[String, AnyRef], issuedCertificates: java.util.List[java.util.Map[String, String]],
                   completedOn: Long, progress: Int, status: Int)

case class EnrolmentData(userId: String, collectionId: String, contextId: String, enrolledDate: Long, active: Boolean,
                         issuedCertificates: java.util.List[java.util.Map[String, String]], completedOn: Long, progress: Int, status: Int)

case class UserActivityData(userId: String, collectionId: String, contextId: String, contentStatus: Map[String, Integer],
                            assessmentStatus: Map[String, Map[String, AnyRef]]) {
  def this() = this("", "", "", Map(), Map())
}