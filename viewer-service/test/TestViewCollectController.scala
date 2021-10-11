import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import com.typesafe.config.Config
import controllers.ViewCollectController
import filter.Attributes.USER_ID
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sunbird.viewer.BaseRequest
import org.sunbird.viewer.actors.ViewCollectActor
import org.sunbird.viewer.core.{CassandraUtil, RedisUtil}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.duration._

class TestViewCollectController extends FlatSpec with Matchers with BeforeAndAfterAll  with MockitoSugar {

    implicit val system = ActorSystem()
    implicit val mockConfig = mock[Config]
    private val configurationMock = mock[Configuration]
    when(configurationMock.underlying).thenReturn(mockConfig)
    implicit val timeout: Timeout = 20.seconds
    implicit val executor = scala.concurrent.ExecutionContext.global
    val redisUtilMock= mock[RedisUtil]
    when(mockConfig.getInt("redis.viewer.db")).thenReturn(1)
    val viewActor = TestActorRef(new ViewCollectActor(mockConfig,mock[CassandraUtil],mock[RedisUtil]) {
        override def receive: Receive = {
            case BaseRequest("start",request: String) => sender () ! start(request)
            case BaseRequest("update",request: String) => sender () ! update(request)
            case BaseRequest("end",request: String) => sender () ! end(request)

        }
    })
    val controller = new ViewCollectController(viewActor, Helpers.stubControllerComponents(),configurationMock)

    "ViewCollectController" should "test the start request with valid request " in {
        val result = controller.start().apply(FakeRequest().withJsonBody(Json.parse("""{"id":"api.view.start","ver":"1.0","request":{"userId":"user_123","contentId":"content_123"}}"""))
          .addAttr(USER_ID,"user_123"))
        Helpers.status(result) should be(Helpers.OK)
    }

    "ViewCollectController" should "test the start request with invalid userid  " in {
        val result = controller.start().apply(FakeRequest().withJsonBody(Json.parse("""{"id":"api.view.start","ver":"1.0","request":{"userId":"user_123","contentId":"content_123"}}""")))
        Helpers.status(result) should be(Helpers.BAD_REQUEST)
    }

    "ViewCollectController" should "test the update request with valid request " in {

        val result = controller.update().apply(FakeRequest().
          withJsonBody(Json.parse("""{"id":"api.view.update","ver":"1.0","request":{"userId":"user_123","contentId":"content_123","progressdetails":{"mimetype":"video","progress":10},"timespent":10}}"""))
          .addAttr(USER_ID,"user_123"))
        Helpers.status(result) should be(Helpers.OK)
    }

    "ViewCollectController" should "test the end request with valid request " in {
        val result = controller.end().apply(FakeRequest().withJsonBody(Json.parse("""{"id":"api.view.end","ver":"1.0","request":{"userId":"user_123","contentId":"content_123"}}"""))
          .addAttr(USER_ID,"user_123"))
        Helpers.status(result) should be(Helpers.OK)
    }

}
