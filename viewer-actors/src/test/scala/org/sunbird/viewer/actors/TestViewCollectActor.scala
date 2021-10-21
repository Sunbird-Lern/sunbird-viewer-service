package org.sunbird.viewer.actors

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.apache.kafka.common.serialization.StringDeserializer
import org.sunbird.viewer.BaseRequest
import org.sunbird.viewer.core._
import org.sunbird.viewer.spec.BaseSpec
import redis.clients.jedis.Jedis

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
class TestViewCollectActor extends BaseSpec {
    var cassandraUtil: CassandraUtil = _
    var kafkaUtil : KafkaUtil = _
    var redisUtil : RedisUtil = _
    var redisConnection : Jedis = _
    var collectRefActor  : TestActorRef[ViewCollectActor]  = _
    implicit val deserializer: StringDeserializer = new StringDeserializer()
    override def beforeAll(): Unit = {
        super.beforeAll()

        cassandraUtil = new CassandraUtil
        kafkaUtil = new KafkaUtil
        redisUtil = new RedisUtil
        redisConnection = redisUtil.getConnection(AppConfig.getInt("redis.viewer.db"))
        collectRefActor = TestActorRef(new ViewCollectActor(config,cassandraUtil,redisUtil,kafkaUtil))
    }
    override def afterAll() : Unit = {
        redisUtil.closePool()
        kafkaUtil.close()
        super.afterAll()
    }

    private implicit val system: ActorSystem = ActorSystem("test-actor-system", config)

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val timeout: Timeout = 10.seconds

    "CollectService" should "test receive function" in {
        val request = """{"id":"api.view.start","ver":"1.0","request":{"userId":"user_123","collectionId":"cc1","contextId":"b1","contentId":"c1"}}"""
        val request1= """{"userId":"user_test1","contentId":"content_test1","progressdetails":{"mimetype":"video","progress":10},"timespent":10}"""
        val request2= """{"userId":"user_test1","contentId":"content_test1","progressdetails":{"mimetype":"video","progress":10}}"""
        collectRefActor.underlyingActor.receive(BaseRequest("start",request))
        collectRefActor.underlyingActor.receive(BaseRequest("update",request1))
        collectRefActor.underlyingActor.receive(BaseRequest("end",request2))
    }


    "CollectService" should "return success response for valid start request" in {
        val request = """{"userId":"user_123","collectionId":"cc1","contextId":"b1","contentId":"c1"}"""
        val response = collectRefActor.underlyingActor.start(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "CollectService" should "return success response for valid start  request without context" in {
        val request = """{"userId":"user_123","contentId":"c1"}"""
        val response = collectRefActor.underlyingActor.start(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "CollectService" should "return success response for valid start request handling all scenarios" in {
        val request = """{"userId":"user2","contentId":"content2","collectionId": "collection2","contextId" :"context2"}"""
        val response = collectRefActor.underlyingActor.start(request)
        redisUtil.resetConnection
        val redisStatus = redisUtil.getConnection(1).hgetAll("user2:content2:collection2:context2")
        redisStatus.get("status") should be("1")
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "CollectService" should "return failed response for all invalid request" in {
        val request = """{"collectionId":"cc1","batchId":"b1","contentId":"c1"}"""
        val response = collectRefActor.underlyingActor.start(request)
        val request1 = Map("collectionId"->"cc1","batchId" -> "b1")
        val response1 = collectRefActor.underlyingActor.start(JSONUtils.serialize(request1))
        response.result.get.apply("request.userId") should be ("Invalid User-authenticated-Token Header")
        response.params.status should be ("failed")
        response1.params.status should be ("failed")
    }

    "CollectService" should "return success response for valid update  request" in {
        val request = """{"userId":"user_test1","contentId":"content_test1","progressdetails":{"mimetype":"video","progress":10},"timespent":10}"""
        val response = collectRefActor.underlyingActor.update(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "CollectService" should "return failed response for invalid update  request" in {
        val request = """{"userId":"user_123","contentId":"content_123","progressdetails":{},"timespent":10}"""
        val response = collectRefActor.underlyingActor.update(request)
        response.responseCode should be("CLIENT_ERROR")
        response.params.status should be("failed")
        response.result.get.contains("request.progressdetails") should be (true)
    }


    "CollectService" should "return success response for valid end  request without context" in {
        val request = """{"userId":"user_test1","contentId":"content_test1"}"""
        val response = collectRefActor.underlyingActor.end(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
        val request1 = """{"userId":"user_test1","contentId":"content_test1","progressdetails":{"mimetype":"video","progress":10}}"""
        val response1 = collectRefActor.underlyingActor.end(request1)
        response.responseCode should be("OK")
        response.params.status should be("successful")
        redisConnection.hgetAll("user_test1:content_test1:content_test1:content_test1")
          .get("progressdetails") should be("{\"mimetype\":\"video\",\"progress\":10}")
    }

    "CollectService" should "return failed response for all invalid end view request" in {
        val request = """{"collectionId":"cc1","batchId":"b1","contentId":"c1"}"""
        val response = collectRefActor.underlyingActor.end(request)
        val request1 = Map("collectionId"->"cc1","batchId" -> "b1")
        val response1 = collectRefActor.underlyingActor.end(JSONUtils.serialize(request1))
        response.result.get.apply("request.userId") should be ("Invalid User-authenticated-Token Header")
        response.params.status should be ("failed")
        response1.params.status should be ("failed")
    }
}
