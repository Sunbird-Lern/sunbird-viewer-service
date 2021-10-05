import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.Timeout
import com.typesafe.config.Config
import controllers.HealthCheckController
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sunbird.viewer.actors.HealthCheckActor
import play.api.Configuration
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.duration._

class TestHealthCheckController extends FlatSpec with Matchers with BeforeAndAfterAll  with MockitoSugar {

    implicit val system = ActorSystem()
    implicit val mockConfig = mock[Config]
    private val configurationMock = mock[Configuration]
    when(configurationMock.underlying).thenReturn(mockConfig)
    implicit val timeout: Timeout = 20.seconds
    implicit val executor = scala.concurrent.ExecutionContext.global
    val healthActor = TestActorRef(new HealthCheckActor() {
        override def receive: Receive = {
            case "checkhealth" => sender() ! getHealthStatus

        }
    })
    val controller = new HealthCheckController(healthActor, Helpers.stubControllerComponents(),configurationMock)

    "HealthCheckController" should "test the health check api  " in {
        val result = controller.getHealthCheckStatus().apply(FakeRequest())
        Helpers.status(result) should be(Helpers.OK)
    }


    "HealthCheckController" should "api services connection" in {
        val result = healthActor.underlyingActor.getServiceHealthStatus(List("redis","cassandra","kafka"))
    }

}
