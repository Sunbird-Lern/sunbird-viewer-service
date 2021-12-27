package org.sunbird.viewer

import org.sunbird.viewer.core.APIResponse

import java.util.UUID

object Models {
}

object ResponseCode extends Enumeration {
  type Code = Value
  val OK, CLIENT_ERROR, SERVER_ERROR = Value
}

object StatusCode extends Enumeration {
  type status = Int
  val START = Value("1")
  val END: StatusCode.Value = Value("2")
}


object Constants {
  val VIEW_START_REQUEST = "api.view.start"
  val VIEW_END_REQUEST = "api.view.end"
  val VIEW_UPDATE_REQUEST = "api.view.update"
  val VIEW_READ_REQUEST = "api.view.read"
  val VIEW_ASSESS_SUBMIT_REQUEST = "api.view.assess.submit"
  val VIEW_ASSESS_READ_REQUEST = "api.view.assess.read"
  val VIEW_CONTEXT_READ_ALL_REQUEST = "api.view.read.context.all"
  val SUNBIRD_COURSES_KEYSPACE = "sunbird_courses"
  val CONTENT_CONSUMPTION_TABLE = "user_content_consumption_new"
  val USER_ENROLMENTS_TABLE = "user_enrolments"
}

object Messages {
  val FIELD_REQUIRED = s"""Field cannot be empty"""
  val USER_TOKEN_REQURIED = "Invalid User-authenticated-Token Header"
  val REQUEST_EMTPY = s"""Request cannot be empty"""
}
// Common Class

case class Params(resmsgid: String, msgid: String, err: String, status: String, errmsg: Map[String, String], client_key: Option[String] = None)

case class BaseResponse(id: String, ver: String, ts: String, params: Params, responseCode: String, result: Option[Map[String, AnyRef]]) extends APIResponse

case class BaseRequest(`type`: String, request: String)

sealed trait BaseViewRequest {
  def userId: String

  def contentId: String

  def collectionId: Option[String]

  def contextId: Option[String]

  def validate(): Map[String, AnyRef] = {
    if (null == userId || userId.isEmpty)
      Map("request.userId" -> Messages.USER_TOKEN_REQURIED)
    else if (null == contentId || contentId.isEmpty)
      Map("request.contentId" -> Messages.FIELD_REQUIRED)
    else
      Map()
  }

  override def toString: String = {
    s"""$userId:$contentId:${collectionId.getOrElse(contentId)}:${contextId.getOrElse(collectionId.getOrElse(contentId))}"""
      .stripMargin
  }

  def validateRequest: Either[Map[String, AnyRef], BaseViewRequest]
}

case class ViewRequestBody(id: String, ver: String, ts: String, request: Map[String, AnyRef], params: Option[Params])


case class ViewRequest(userId: String, contentId: String, collectionId: Option[String], contextId: Option[String])
  extends BaseViewRequest {

  override def validateRequest: Either[Map[String, AnyRef], ViewRequest] = {
    val validationErrors = validate()
    if (validationErrors.nonEmpty)
      Left(validationErrors)
    else
      Right(ViewRequest(userId, contentId, Some(collectionId.getOrElse(contentId)),
        Some(contextId.getOrElse(collectionId.getOrElse(contentId)))))
  }
}

case class UpdateRequest(userId: String, contentId: String, collectionId: Option[String], contextId: Option[String],
                         progressdetails: Map[String, Any], timespent: Double) extends BaseViewRequest {
  override def validateRequest: Either[Map[String, AnyRef], UpdateRequest] = {
    val validationErrors = validate()
    if (validationErrors.nonEmpty)
      Left(validationErrors)
    else if (progressdetails.isEmpty)
      Left(Map("request.progressdetails" -> Messages.FIELD_REQUIRED))
    else {
      Right(UpdateRequest(userId, contentId, Some(collectionId.getOrElse(contentId)),
        Some(contextId.getOrElse(collectionId.getOrElse(contentId))), progressdetails, timespent))
    }
  }
}

case class EndRequest(userId: String, contentId: String, collectionId: Option[String], contextId: Option[String],
                      progressdetails: Option[Map[String, Any]]) extends BaseViewRequest {
  override def validateRequest: Either[Map[String, AnyRef], EndRequest] = {
    val validationErrors = validate()
    if (validationErrors.nonEmpty)
      Left(validationErrors)
    else {
      Right(EndRequest(userId, contentId, Some(collectionId.getOrElse(contentId)),
        Some(contextId.getOrElse(collectionId.getOrElse(contentId))),
        progressdetails))
    }
  }
}


case class ReadContent(userId: String, contents: List[ContentSpec])

case class ReadAllContent(userId: String, allContents: List[ContentSpec])

case class ContentSpec(contentId: String, collectionId: String, contextId: String, status: String, progressDetails: Map[String, AnyRef])

case class ReadAllRequest(userId: String, contentId: Option[String] = None, collectionId: Option[String] = None) {
  def validateRequest(): Either[Map[String, String], ReadAllRequest] = {
    if (null == userId || userId.isEmpty)
      Left(Map("request.userId" -> Messages.USER_TOKEN_REQURIED))
    else if (contentId.getOrElse("").isEmpty && collectionId.getOrElse("").isEmpty)
      Left(Map("request" -> Messages.REQUEST_EMTPY))
    else
      Right(ReadAllRequest(userId, contentId, collectionId))
  }

  override def toString: String = {
    s"""$userId:${contentId.getOrElse(collectionId)}"""
  }
}

case class AssessSpec(assessmentTs: Long, userId: String, contentId: String, collectionId: Option[String], contextId: Option[String],
                      attemptId: String, events: List[Map[String, AnyRef]]) extends BaseViewRequest {
  override def validateRequest: Either[Map[String, AnyRef], AssessSpec] = {
    val validationErrors = validate()
    if (validationErrors.nonEmpty)
      Left(validationErrors)
    else if (attemptId.isEmpty)
      Left(Map("request.attemptId" -> Messages.FIELD_REQUIRED))
    else {
      Right(AssessSpec(assessmentTs,userId, contentId, Some(collectionId.getOrElse(contentId)),
        Some(contextId.getOrElse(collectionId.getOrElse(contentId))),attemptId,events))
    }
  }
}

case class ViewAssessRequest(userId: String,assessments: List[AssessSpec]) {
   def validateRequest: Either[Map[String, AnyRef], ViewAssessRequest] = {
     if (null == userId || userId.isEmpty)
       Left(Map("request.userId" -> Messages.USER_TOKEN_REQURIED))
    else if (assessments.isEmpty)
      Left(Map("request.assessments" -> Messages.FIELD_REQUIRED))
    else {
      Right(ViewAssessRequest(userId, assessments))
    }
  }
}
case class Question(id: String, maxscore: Double, params: List[Map[String, Any]], title: String, `type`: String, desc: String)
case class QuestionData(resvalues: List[Map[String, Any]], duration: Double, score: Double, item: Question)
case class AssessEvent(ets: Long, edata: QuestionData)

case class ContentEndEvent(eid: String = "BE_JOB_REQUEST", ets: Long = System.currentTimeMillis(),
                           mid: String = UUID.randomUUID.toString, actor: TypeId, context: Context, `object`: TypeId,
                           edata: EData, action: String, iteration: Int,
                           batchId: String, userId: String, courseId: String)

case class TypeId(`type`: String, id: String)

case class Context(pdata: PData)

case class PData(ver: String, id: String)

case class EData(contents: List[Content])

case class Content(contentId: String, status: Int)
