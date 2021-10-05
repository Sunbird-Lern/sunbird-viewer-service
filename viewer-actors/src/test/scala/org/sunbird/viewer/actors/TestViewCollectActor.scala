package org.sunbird.viewer.actors

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.apache.kafka.common.serialization.StringDeserializer
import org.sunbird.viewer.BaseRequest
import org.sunbird.viewer.core.{CassandraUtil, JSONUtils, KafkaUtil}
import org.sunbird.viewer.spec.BaseSpec

import java.util.concurrent.TimeoutException
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class TestViewCollectActor extends BaseSpec {
    var cassandraConnector: CassandraUtil = _
    var kafkaConnector : KafkaUtil = _
    var collectRefActor  : TestActorRef[ViewCollectActor]  = _
    implicit val deserializer: StringDeserializer = new StringDeserializer()
    override def beforeAll(): Unit = {
        super.beforeAll()
        cassandraConnector = new CassandraUtil
        kafkaConnector = new KafkaUtil()
        collectRefActor = TestActorRef(new ViewCollectActor(cassandraConnector))
    }
    override def afterAll() : Unit = {
        super.afterAll()
        kafkaConnector.close()
    }

    private implicit val system: ActorSystem = ActorSystem("test-actor-system", config)

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val timeout: Timeout = 10.seconds

    "CollectService" should "test receive function" in {
            val request = """{"id":"api.view.start","ver":"1.0","request":{"userId":"user_123","collectionId":"cc1","contextId":"b1","contentId":"c1"}}"""
        collectRefActor.underlyingActor.receive(BaseRequest("start",request))
    }

    "CollectService" should "return success response for valid start request" in {
        val request = """{"userId":"user_123","collectionId":"cc1","contextId":"b1","contentId":"c1"}"""
        val response = collectRefActor.underlyingActor.start(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "CollectService" should "return success response for valid start  request without batch" in {
        val request = """{"userId":"user_123","contentId":"c1"}"""
        val response = collectRefActor.underlyingActor.start(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "CollectService" should "return failed response for all invalid request" in {
        val request = """{"collectionId":"cc1","batchId":"b1","contentId":"c1"}"""
        val response = collectRefActor.underlyingActor.start(request)
        val request1 = Map("collectionId"->"cc1","batchId" -> "b1")
        val response1 = collectRefActor.underlyingActor.start(JSONUtils.serialize(request1))
        response.result.get.apply("request.userId") should be ("cannot be empty")
        response.params.status should be ("failed")
        response1.params.status should be ("failed")
    }



}
