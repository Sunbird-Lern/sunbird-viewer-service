package org.sunbird.viewer.util

import com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker
import com.datastax.driver.core.querybuilder.{QueryBuilder => QB}
import org.sunbird.viewer.Constants

import java.util.Date

/**
 * STATEMENTS
 * INSERT INTO sunbird_courses.user_content_consumption_new (userid,contentid,status,progress,courseid,batchid) VALUES (?,?,?,?,?,?) IF NOT EXISTS;
 * UPDATE sunbird_courses.user_content_consumption_new SET progress=? WHERE userid=? AND contentid=?;
 */

object QueryUtil {

  def getInsertViewStartStatement(table: String, trackable: Boolean): String = {
    var query = QB.insertInto(Constants.SUNBIRD_COURSES_KEYSPACE, table).ifNotExists()
      .value("userid", bindMarker()).value("contentid", bindMarker())
      .value("status", bindMarker()).value("progress", bindMarker())
      .value("last_updated_time", new Date())
    if (trackable)
      query = query.value("batchid", bindMarker()).value("courseid", bindMarker())
    query.toString
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
      .and(QB.eq("contentid", bindMarker())).toString
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
