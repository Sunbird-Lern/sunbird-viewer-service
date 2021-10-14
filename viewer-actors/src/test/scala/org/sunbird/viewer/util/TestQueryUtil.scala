package org.sunbird.viewer.util

import org.scalatest.{FlatSpec, Matchers}
import org.sunbird.viewer.ViewerSummaryRequest

class TestQueryUtil extends FlatSpec with Matchers {
  
  "get enrolments with only userId" should "return query" in {
    val query = QueryUtil.getEnrolments(ViewerSummaryRequest("user01", Option(null), Option(null)))
    val expectedQuery = "SELECT * FROM sunbird_courses.user_enrolments WHERE userid='user01';"
    assert(query.contentEquals(expectedQuery))
  }

  "get enrolments with only userId, collectionId and contextId" should "return query" in {
    val query = QueryUtil.getEnrolments(ViewerSummaryRequest("user01", Option("do_123"), Option("0123")))
    val expectedQuery = "SELECT * FROM sunbird_courses.user_enrolments WHERE userid='user01' AND courseid='do_123' AND batchid='0123';"
    assert(query.contentEquals(expectedQuery))
  }

  "get user activites" should "return query" in {
    val query = QueryUtil.getUserActivities("user01", List("do_123", "do_234"))
    val expectedQuery = "SELECT * FROM sunbird_courses.user_activity_agg WHERE activity_type='Course' AND user_id='user01' AND activity_id IN ('do_123','do_234');"
    assert(query.contentEquals(expectedQuery))
  }

}
