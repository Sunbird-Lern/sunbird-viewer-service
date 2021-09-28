package org.sunbird.viewer.platform

import com.datastax.driver.core.exceptions.DriverException
import com.datastax.driver.core._
import org.slf4j.LoggerFactory


class CassandraUtil() {

  private val logger = LoggerFactory.getLogger("CassandraConnectorLogger")
  val host  = AppConfig.getString("cassandra.connection.host")
  val port = AppConfig.getString("cassandra.connection.port").toInt
  val options : QueryOptions = new QueryOptions()
  val cluster = {
    Cluster.builder()
      .addContactPoint(host)
      .withPort(port)
      .withQueryOptions(options.setConsistencyLevel(ConsistencyLevel.QUORUM))
      .withoutJMXReporting()
      .build()
  }
  private val session = cluster.connect()


  def executeQuery(query: String): Boolean = {
    val rs: ResultSet = session.execute(query)
    rs.wasApplied
  }

  def executeStatement(query: String, bindList:List[AnyRef]): Boolean = {
    val rs: ResultSet = session.execute(session.prepare(query).bind(bindList :_*))
    rs.wasApplied
  }


  def find(query: String,bindList: List[AnyRef]): java.util.List[Row] = {
    try {
      val rs: ResultSet = session.execute(session.prepare(query).bind(bindList : _*))
      rs.all
    } catch {
      case ex: DriverException =>
        logger.info(s"Failed cassandra query is ${query}")
        ex.printStackTrace()
        throw ex
    }
  }

  def checkConnection() = {
    try {
      session.execute("SELECT now() FROM system.local").wasApplied
      cluster.close()
      true
    }
    catch {
      case ex : Exception => throw new Exception("Cassandra :" + ex.getMessage)
      false
    }
  }

}
