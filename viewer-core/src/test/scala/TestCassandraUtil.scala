import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.bindMarker
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.sunbird.viewer.core.CassandraUtil

class TestCassandraUtil extends FlatSpec  with Matchers with BeforeAndAfterAll {
 var cassandraUtil : CassandraUtil = _
  override def beforeAll(): Unit ={
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(80000L)
    cassandraUtil = new CassandraUtil()
    val tableQuery = "CREATE TABLE IF NOT EXISTS sunbird.content_consumption (userid text,contentid text,collectionid text,contextid text,status int,PRIMARY KEY (userid, contentid, collectionid, contextid))"
    val ksQuery = "CREATE KEYSPACE IF NOT EXISTS sunbird WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'}"
    cassandraUtil.executeQuery(ksQuery)
    cassandraUtil.executeQuery(tableQuery)
    cassandraUtil.executeQuery("INSERT INTO sunbird.content_consumption (userid,contentid,collectionid,contextid,status) VALUES ('u1', 'c1','cc1','b1',1)")
  }
  override def afterAll(): Unit ={
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }




  "CassandraUtil" should "test the execute statement method" in {
    val stmt  =QueryBuilder.insertInto("sunbird", "content_consumption").ifNotExists()
      .value("userid", "user1").value("contentid", "content1")
      .value("collectionId","collection1").value("contextid", "context1")
      .value("status", bindMarker()).toString
    val status:Int=0
    cassandraUtil.executeStmt(stmt,List(status.asInstanceOf[AnyRef]))
  }

  "CassandraUtil" should "test the execute find method" in {
    val stmt  =QueryBuilder.select.from("sunbird","content_consumption")
      .where(QueryBuilder.eq("userid",bindMarker()))
      .and(QueryBuilder.eq("contentid",bindMarker()))
      .and(QueryBuilder.eq("collectionid",bindMarker()))
      .and(QueryBuilder.eq("contextid",bindMarker())).toString
    cassandraUtil.find(stmt,List("u1","c1","cc1","b1")).size() should be (1)
  }

  "CassandraUtil" should "test the check connection" in {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    intercept[Exception] {
      cassandraUtil.checkConnection() should be(false)
    }
  }
}
