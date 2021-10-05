package org.sunbird.viewer.spec

import com.datastax.driver.core.Cluster
import com.typesafe.config.ConfigFactory
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.FileCQLDataSet
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sunbird.viewer.core.AppConfig
import org.cassandraunit.utils.EmbeddedCassandraServerHelper


class BaseSpec extends FlatSpec with Matchers with BeforeAndAfterAll with MockitoSugar {
  implicit val config = ConfigFactory.load()

  override def beforeAll() {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(80000L)
    val cluster = {
      Cluster.builder()
        .addContactPoint(AppConfig.getString("cassandra.connection.host"))
        .withPort(AppConfig.getString("cassandra.connection.port").toInt)
        .withoutJMXReporting()
        .build()
    }
    val session = cluster.connect()
    val dataLoader = new CQLDataLoader(session)
    dataLoader.load(new FileCQLDataSet(getClass.getResource("/data.cql").getPath, true, true))
  }

  override def afterAll(): Unit = {

    try {
      EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    } catch {
      case ex: Exception =>
        println("error while stopping embed cassandra",ex)
    }
  }

}
