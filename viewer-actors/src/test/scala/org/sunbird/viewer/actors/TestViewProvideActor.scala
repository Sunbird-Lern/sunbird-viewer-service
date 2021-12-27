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
import scala.collection.JavaConversions._
class TestViewProvideActor extends BaseSpec {
    var cassandraUtil: CassandraUtil = _
    var kafkaConnector : KafkaUtil = _
    var redisUtil : RedisUtil = _
    var redisConnection : Jedis = _
    var readRefActor  : TestActorRef[ViewProvideActor]  = _
    implicit val deserializer: StringDeserializer = new StringDeserializer()
    override def beforeAll(): Unit = {
        super.beforeAll()

        cassandraUtil = new CassandraUtil
        kafkaConnector = new KafkaUtil
        redisUtil = new RedisUtil
        redisConnection = redisUtil.getConnection(AppConfig.getInt("redis.viewer.db"))
        readRefActor = TestActorRef(new ViewProvideActor(config,cassandraUtil,redisUtil))
    }
    override def afterAll() : Unit = {
        redisUtil.closePool()
        kafkaConnector.close()
        super.afterAll()
    }

    private implicit val system: ActorSystem = ActorSystem("test-actor-system", config)

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val timeout: Timeout = 10.seconds


    "ProvideService" should "return success response for valid read request" in {
        redisConnection.hmset("user_123:content_123:content_123:content_123",mapAsJavaMap(Map("status" -> "2",
            "progressdetails" -> "{\"mimetype\":\"video\",\"progress\":10}")))
        val request = """{"userId":"user_123","contentId":"content_123"}"""
        val response = readRefActor.underlyingActor.read(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "ProvideService" should "return success response for valid read in cache miss request" in {
        val request = """{"userId":"user_test2","contentId":"content_test1"}"""
        val response = readRefActor.underlyingActor.read(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

    "ProvideService" should "return success response for valid ALL read in cache miss request" in {
        val request = """{"userId":"user_test2","contentId":"content_test1"}"""
        val response = readRefActor.underlyingActor.readAll(request)
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

}
