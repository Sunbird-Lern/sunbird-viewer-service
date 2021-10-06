package modules

import akka.routing.FromConfig
import com.google.inject.AbstractModule
import org.sunbird.viewer.actors.{HealthCheckActor, StartUpdateActor, ViewerSummaryActor}
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorInjector extends AbstractModule with AkkaGuiceSupport {
  // $COVERAGE-OFF$ Disabling scoverage for INJECTOR
  override def configure(): Unit = {
    val actorConfig = new FromConfig()

    bindActor[HealthCheckActor](name = "health-check-actor", _.withRouter(actorConfig))
    bindActor[StartUpdateActor](name ="view-start-update-actor")
    bindActor[ViewerSummaryActor](name = "view-summary-actor", _.withRouter(actorConfig))
    /*bindActor[ViewEndActor](name = "view-end-actor")*/
  }
}
