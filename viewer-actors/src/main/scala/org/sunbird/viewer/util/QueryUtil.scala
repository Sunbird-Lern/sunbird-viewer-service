package org.sunbird.viewer.util

import com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker
import com.datastax.driver.core.querybuilder.{Insert, Update, QueryBuilder => QB}
import org.sunbird.viewer.{BaseViewRequest, Constants}

import java.util.Date

/**
 * STATEMENTS
 * INSERT INTO sunbird_courses.user_content_consumption_new (userid,contentid,collectionid,batchid,status,progress) VALUES (?,?,?,?,?,?) IF NOT EXISTS;
 * UPDATE sunbird_courses.user_content_consumption_new SET progressdetails=?  WHERE userid=? AND contentid=? and collectionid=? and contextid=?;
 * UPDATE sunbird_courses.user_content_consumption_new SET status=? WHERE userid=? AND contentid=? and collectionid=? and contextid=?;
 */

object QueryUtil {

  def getBaseInsert(table:String,request:BaseViewRequest): Insert = {
    QB.insertInto(Constants.SUNBIRD_COURSES_KEYSPACE, table).ifNotExists()
      .value("userid", request.userId).value("contentid", request.contentId)
      .value("collectionId", request.collectionId.get).value("contextid", request.contextId.get)
  }
  def getBaseUpdate(request:BaseViewRequest,update: Update.Assignments): Update.IfExists = {
    update
      .where(QB.eq("userid", request.userId))
      .and(QB.eq("contentid",request.contentId))
      .and(QB.eq("collectionid", request.collectionId.get))
      .and(QB.eq("contextid", request.contextId.get)).ifExists()
  }

  def startViewStmt(table: String,request: BaseViewRequest): String = {
      getBaseInsert(table,request)
      .value("status", bindMarker())
      .value("last_updated_time", new Date()).toString
  }

  def updateViewStmt(table: String,request: BaseViewRequest): String = {
    getBaseUpdate(request,QB.update(Constants.SUNBIRD_COURSES_KEYSPACE, table)
      .`with`(QB.set("progressdetails", bindMarker()))
      .and(QB.set("last_updated_time", new Date()))).toString

  }

  def endViewStmt(table: String,request: BaseViewRequest,progress:Boolean): String = {
    val query =  QB.update(Constants.SUNBIRD_COURSES_KEYSPACE, table)
      .`with`(QB.set("status", bindMarker())).and(QB.set("last_completed_time", new Date()))
      .and(QB.set("last_updated_time", new Date()))
    val finalQuery = if(progress) query.and(QB.set("progressdetails", bindMarker())) else query
    getBaseUpdate(request,finalQuery).toString
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

}
