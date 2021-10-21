import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import com.typesafe.config.Config
import controllers.ViewReadController
import filter.Attributes.USER_ID
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sunbird.viewer.BaseRequest
import org.sunbird.viewer.actors.ViewProvideActor
import org.sunbird.viewer.core.{CassandraUtil, RedisUtil}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.typedmap.TypedKey
import play.api.test.{FakeRequest, Helpers}
import redis.clients.jedis.Jedis

import scala.concurrent.duration._

class TestViewProvideController extends FlatSpec with Matchers with BeforeAndAfterAll  with MockitoSugar {

    implicit val system = ActorSystem()
    implicit val mockConfig = mock[Config]
    private val configurationMock = mock[Configuration]
    when(configurationMock.underlying).thenReturn(mockConfig)
    implicit val timeout: Timeout = 20.seconds
    implicit val executor = scala.concurrent.ExecutionContext.global
    when(mockConfig.getInt("redis.viewer.db")).thenReturn(1)
    val redisMock = mock[RedisUtil]
    when(redisMock.getConnection(1)).thenReturn(mock[Jedis])
    val viewActor = TestActorRef(new ViewProvideActor(mockConfig,mock[CassandraUtil],redisMock) {
        override def receive: Receive = {
            case BaseRequest("read",request: String) => sender () ! read(request)
            case BaseRequest("readall",request: String) => sender () ! readAll(request)
        }
    })
    val controller = new ViewReadController(viewActor, Helpers.stubControllerComponents(),configurationMock)

    "ViewReadController" should "test the start request with valid request " in {
        val result = controller.read().apply(FakeRequest().withJsonBody(Json.parse("""{"id":"api.view.read","ver":"1.0","request":{"userId":"user_123","contentId":"content_123"}}"""))
          .addAttr(USER_ID,"user_123"))
        Helpers.status(result) should be(Helpers.OK)
    }

    "ViewReadController" should "test the start request with invalid userid" in {
        val result = controller.read().apply(FakeRequest().withJsonBody(Json.parse("""{"id":"api.view.readall","ver":"1.0","request":{"userId":"user_123","contentId":"content_123"}}""")))
        Helpers.status(result) should be(Helpers.BAD_REQUEST)
    }

    "ViewReadController" should "test the update request with valid request " in {
        val result = controller.read().apply(FakeRequest("POST","/v1/view/read?context=all").
          withJsonBody(Json.parse("""{"id":"api.view.update","ver":"1.0","request":{"userId":"user_123","contentId":"content_123"}}"""))
          .addAttr(USER_ID,"user_123"))
        Helpers.status(result) should be(Helpers.OK)
    }


}
