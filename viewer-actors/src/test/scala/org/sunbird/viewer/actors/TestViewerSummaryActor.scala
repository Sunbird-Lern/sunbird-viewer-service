package org.sunbird.viewer.actors

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.apache.kafka.common.serialization.StringDeserializer
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito._
import org.sunbird.viewer.core.{CassandraUtil, HTTPResponse, HttpUtil, KafkaUtil}
import org.sunbird.viewer.spec.BaseSpec

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

class TestViewerSummaryActor extends BaseSpec {

  var cassandraConnector: CassandraUtil = _
  var kafkaConnector : KafkaUtil = _
  var summaryRefActor  : TestActorRef[ViewerSummaryActor]  = _
  var httpUtil:HttpUtil = _
  implicit val deserializer: StringDeserializer = new StringDeserializer()
  override def beforeAll(): Unit = {
    super.beforeAll()
    cassandraConnector = new CassandraUtil
    kafkaConnector = new KafkaUtil()
    httpUtil = mock[HttpUtil]
    summaryRefActor = TestActorRef(new ViewerSummaryActor(cassandraConnector, httpUtil))
  }
  override def afterAll() : Unit = {
    super.afterAll()
    kafkaConnector.close()
  }

  private implicit val system: ActorSystem = ActorSystem("test-actor-system", config)

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  implicit val timeout: Timeout = 10.seconds

  

  "summary List" should "return valid response" in {
    when(httpUtil.post(ArgumentMatchers.eq("https://dev.sunbirded.org/api/content/v1/search"), anyString(), ArgumentMatchers.eq(Map[String, String]("Content-Type" -> "application/json")))).thenReturn(getSearchResponse())
    val response = summaryRefActor.underlyingActor.summaryList("user01")
    response.responseCode should be ("OK")
    response.params.status should be ("successful")
  }

  "summary Read" should "return valid response" in {
    when(httpUtil.post(ArgumentMatchers.eq("https://dev.sunbirded.org/api/content/v1/search"), anyString(), ArgumentMatchers.eq(Map[String, String]("Content-Type" -> "application/json")))).thenReturn(getSearchResponse())
    val response = summaryRefActor.underlyingActor.summaryRead("""{"userId": "user01", "collectionId": "cc01", "contextId": "b01"}""")
    response.responseCode should be ("OK")
    response.params.status should be ("successful")
  }


  def getSearchResponse(): HTTPResponse = {
    HTTPResponse(200, "{\"id\":\"api.content.search\",\"ver\":\"1.0\",\"ts\":\"2021-10-05T14:33:14.455Z\",\"params\":{\"resmsgid\":\"2c6c5e70-25e9-11ec-b008-1be49ecea2c1\",\"msgid\":\"2c6a62a0-25e9-11ec-bba3-19791cd4f584\",\"status\":\"successful\",\"err\":null,\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"count\":1,\"content\":[{\"channel\":\"01269878797503692810\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"leafNodes\":[\"do_21334861113830604811351\"],\"objectType\":\"Content\",\"primaryCategory\":\"Course\",\"trackable\":{\"enabled\":\"Yes\",\"autoBatch\":\"Yes\"},\"identifier\":\"cc01\",\"mediaType\":\"content\",\"license\":\"CC BY 4.0\",\"size\":5338,\"lastPublishedOn\":\"2021-09-23T13:43:45.066+0000\",\"IL_FUNC_OBJECT_TYPE\":\"Collection\",\"name\":\"NEW Course 23\",\"status\":\"Live\",\"code\":\"org.sunbird.NdraFr\",\"prevStatus\":\"Processing\",\"description\":\"Enter description for Course\",\"createdOn\":\"2021-09-23T13:42:25.394+0000\",\"reservedDialcodes\":\"{\\\"L2E2H1\\\":0}\",\"batches\":[{\"createdFor\":[\"01269878797503692810\"],\"endDate\":\"2021-10-07\",\"name\":\"NEW Course 23\",\"enrollmentType\":\"open\",\"batchId\":\"01337266096502374442\",\"enrollmentEndDate\":\"2021-10-05\",\"startDate\":\"2021-09-23\",\"status\":1}],\"lastUpdatedOn\":\"2021-09-23T13:43:44.615+0000\",\"IL_SYS_NODE_TYPE\":\"DATA_NODE\",\"pkgVersion\":1,\"versionKey\":\"1632404624615\",\"leafNodesCount\":1,\"IL_UNIQUE_ID\":\"do_2133726580358266881351\"}]}}")
  }

}
