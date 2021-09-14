package modules

import akka.routing.FromConfig
import com.google.inject.AbstractModule
import org.sunbird.viewer.service.{HealthCheckService}
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorInjector extends AbstractModule with AkkaGuiceSupport {
  // $COVERAGE-OFF$ Disabling scoverage for INJECTOR
  override def configure(): Unit = {
    val actorConfig = new FromConfig()

    bindActor[HealthCheckService](name = "health-check-actor")
    /*bindActor[ViewCollectService](name ="view-collect-actor")
    bindActor[ViewProvideService](name = "view-provide-actor")*/
  }
}
