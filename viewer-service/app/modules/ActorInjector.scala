package modules

import akka.routing.FromConfig
import com.google.inject.AbstractModule
import org.sunbird.viewer.actors.{HealthCheckActor}
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorInjector extends AbstractModule with AkkaGuiceSupport {
  // $COVERAGE-OFF$ Disabling scoverage for INJECTOR
  override def configure(): Unit = {
    val actorConfig = new FromConfig()

    bindActor[HealthCheckActor](name = "health-check-actor", _.withRouter(actorConfig))
    /*bindActor[ViewStartUpdateActor](name ="view-start-update-actor")
    bindActor[ViewEndActor](name = "view-end-actor")*/
  }
}
