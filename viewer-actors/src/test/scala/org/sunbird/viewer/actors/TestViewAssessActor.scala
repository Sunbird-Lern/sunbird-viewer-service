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

class TestViewAssessActor extends BaseSpec {
    var cassandraUtil: CassandraUtil = _
    var kafkaUtil : KafkaUtil = _
    var redisUtil : RedisUtil = _
    var redisConnection : Jedis = _
    var relationCache : Jedis = _
    var collectRefActor  : TestActorRef[ViewAssessActor]  = _
    implicit val deserializer: StringDeserializer = new StringDeserializer()
    override def beforeAll(): Unit = {
        super.beforeAll()

        cassandraUtil = new CassandraUtil
        kafkaUtil = new KafkaUtil
        redisUtil = new RedisUtil
        val courseLeafNodes_1 = Map("collection_1:collection_1:leafnodes" -> "content_1")
        val courseLeafNodes_2 = Map("collection_2:collection_2:leafnodes" -> "content_2")
        val leafNodesList = List(courseLeafNodes_1, courseLeafNodes_2)
        relationCache = redisUtil.getConnection(AppConfig.getInt("redis.relation.db"))
        leafNodesList.map(nodes => {
            nodes.map(node =>{
                relationCache.sadd(node._1,node._2)
            })
        })


        redisConnection = redisUtil.getConnection(AppConfig.getInt("redis.viewer.db"))
        collectRefActor = TestActorRef(new ViewAssessActor(config,cassandraUtil,redisUtil,kafkaUtil))
    }
    override def afterAll() : Unit = {
        redisUtil.closePool()
        kafkaUtil.close()
        super.afterAll()
    }

    private implicit val system: ActorSystem = ActorSystem("test-actor-system", config)

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val timeout: Timeout = 10.seconds



    "AssessService" should "return success response for valid submit request" in {
        val request = """{"userId":"c4242ad3-f19c-4d5e-a442-d4d4005eba46","assessments":[{"assessmentTs":1621247557820,"collectionId":"collection_1","userId":"c4242ad3-f19c-4d5e-a442-d4d4005eba46","attemptId":"9b520f8b329b3169f439ec587746ceee","contentId":"content_1","events":[{"eid":"ASSESS","ets":1621247568145,"ver":"3.1","mid":"ASSESS:074345f9be48f3f55d05ef92f5459d37","actor":{"id":"c4242ad3-f19c-4d5e-a442-d4d4005eba46","type":"User"},"context":{"channel":"0127920475840593920","pdata":{"id":"staging.sunbird.portal","ver":"3.9.0","pid":"sunbird-portal.contentplayer"},"env":"contentplayer","sid":"8Y8y6k-os-kmY3tQmm36cYEe5EHyUErz","did":"0a6c37034cfe5d7ff498a69c57cc3758","cdata":[{"id":"do_2132818357077442561633","type":"course"},{"type":"batch","id":"01328184204133990415"},{"id":"9fc87ef893f17a8b4e2783e63965ba3a","type":"ContentSession"},{"id":"cad79897f0fcef9b444762703c5e9e09","type":"PlaySession"}],"rollup":{"l1":"01328184204133990415"}},"object":{"id":"do_2132720527261450241456","type":"Content","ver":"1","rollup":{"l1":"do_2132720527261450241456","l2":"do_2132720536736808961457"}},"tags":["01328184204133990415"],"edata":{"item":{"id":"do_2132720527261450241456","maxscore":10,"type":"mcq","exlength":0,"params":[{"1":"{\"text\":\"4\\n\"}"},{"2":"{\"text\":\"3\\n\"}"},{"3":"{\"text\":\"8\\n\"}"},{"4":"{\"text\":\"10\\n\"}"},{"answer":"{\"correct\":[\"3\"]}"}],"uri":"","title":"Copy of - 2 +2 is..? mark ia 10\n","mmc":[],"mc":[],"desc":""},"index":1,"pass":"No","score":0,"resvalues":[{"4":"{\"text\":\"10\\n\"}"}],"duration":11}}]}]}"""
        val response = collectRefActor.underlyingActor.submit(request)
        println("the response", response.result.get)
        println("the redis", redisConnection.hgetAll("c4242ad3-f19c-4d5e-a442-d4d4005eba46:content_1:collection_1:collection_1"))
        response.responseCode should be("OK")
        response.params.status should be("successful")
    }

}
