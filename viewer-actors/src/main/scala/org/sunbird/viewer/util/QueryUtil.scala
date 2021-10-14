package org.sunbird.viewer.util

import com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker
import com.datastax.driver.core.querybuilder.{Insert, QueryBuilder => QB}
import org.sunbird.viewer.{BaseViewRequest, Constants, ViewerSummaryRequest}

import java.util.Date
import scala.collection.JavaConverters._

/**
 * STATEMENTS
 * INSERT INTO sunbird_courses.user_content_consumption_new (userid,contentid,collectionid,batchid,status,progress) VALUES (?,?,?,?,?,?) IF NOT EXISTS;
 * UPDATE sunbird_courses.user_content_consumption_new SET progress=? WHERE userid=? AND contentid=? and collectionid=? and contextid=?;
 */

object QueryUtil {

  def getBaseInsert(table:String,request:BaseViewRequest): Insert = {
    QB.insertInto(Constants.SUNBIRD_COURSES_KEYSPACE, table).ifNotExists()
      .value("userid", request.userId).value("contentid", request.contentId)
      .value("collectionId", request.collectionId.get).value("contextid", request.contextId.get)
  }

  def getInsertViewStartStatement(table: String,request: BaseViewRequest): String = {
      getBaseInsert(table,request)
      .value("status", bindMarker()).value("progress", bindMarker())
      .value("last_updated_time", new Date()).toString
  }

  def getUpdateViewUpdateStatement(table: String): String = {
    QB.update(Constants.SUNBIRD_COURSES_KEYSPACE, table)
      .`with`(QB.set("progress", bindMarker())).and(QB.set("last_updated_time", new Date()))
      .where(QB.eq("userid", bindMarker()))
      .and(QB.eq("contentid", bindMarker())).toString
  }

  def getUpdateViewEndStatement(table: String): String = {
    QB.update(Constants.SUNBIRD_COURSES_KEYSPACE, table)
      .`with`(QB.set("progress", bindMarker())).and(QB.set("status", bindMarker()))
      .and(QB.set("last_updated_time", new Date()))
      .where(QB.eq("userid", bindMarker()))
      .and(QB.eq("contentid", bindMarker())).ifExists().toString
  }

  def getUpdateEndEnrolStatement(table: String): String = {
    QB.update(Constants.SUNBIRD_COURSES_KEYSPACE, table)
      .`with`(QB.appendAll("contentstatus", bindMarker()))
      .where(QB.eq("userid", bindMarker()))
      .and(QB.eq("courseid", bindMarker()))
      .and(QB.eq("batchid", bindMarker())).toString
  }

  def getViewReadStatement(table:String) : String = {
    QB.select.from(Constants.SUNBIRD_COURSES_KEYSPACE,table)
      .where(QB.eq("userid",bindMarker()))
      .and(QB.eq("courseid",bindMarker()))
      .and(QB.eq("batchid",bindMarker())).toString
  }
  
  def getEnrolments(request: ViewerSummaryRequest): String = {
    var select = QB.select.from(Constants.SUNBIRD_COURSES_KEYSPACE, Constants.USER_ENROLMENTS_TABLE)
      .where(QB.eq("userid", request.userId))
    select = request.collectionId.map(x => select.and(QB.eq("courseid", x))).getOrElse(select)
    select = request.contextId.map(x => select.and(QB.eq("batchid", x))).getOrElse(select)
    select.toString
  }

  /**
   * To fetch user activity agg
   * cannot include context_id to in clause, as cassandra throws error
   * @param userId
   * @param collectionIds
   * @param contextIds
   * @return
   */
  def getUserActivities(userId: String, collectionIds: List[String]): String = {
    val select = QB.select.from(Constants.SUNBIRD_COURSES_KEYSPACE, Constants.USER_ACTIVITY_TABLE)
      .where(QB.eq("activity_type","Course")).and(QB.eq("user_id", userId))
      .and(QB.in("activity_id", collectionIds.asJava))
    select.toString
  }
}
