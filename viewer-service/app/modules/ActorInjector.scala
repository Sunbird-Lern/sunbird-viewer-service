package modules

import akka.routing.FromConfig
import com.google.inject.AbstractModule
import org.sunbird.auth.verifier.KeyManager
import org.sunbird.viewer.actors.{HealthCheckActor, ViewCollectActor}
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorInjector extends AbstractModule with AkkaGuiceSupport {
  // $COVERAGE-OFF$ Disabling scoverage for INJECTOR
  override def configure(): Unit = {
    KeyManager.init()
    val actorConfig = new FromConfig()

    bindActor[HealthCheckActor](name = "health-check-actor", _.withRouter(actorConfig))
    bindActor[ViewCollectActor](name ="view-collect-actor")
    /*bindActor[ViewEndActor](name = "view-end-actor")*/
  }
}
