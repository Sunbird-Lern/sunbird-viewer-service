

import controllers.BaseController
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import play.api.mvc.Result


class TestBaseController extends FlatSpec with Matchers with BeforeAndAfterAll with MockitoSugar {


  "TestBaseController" should "Should return success status when code is OK " in {
    val controller = new BaseController(null, null)
    val result :Result = controller.result("OK","Success")
    result.header.status should be (200)
  }

  "TestBaseController" should "Should return bad request status when code is CLIENT_ERROR " in {
    val controller = new BaseController(null, null)
    val result :Result = controller.result("CLIENT_ERROR","Error")
    result.header.status should be (400)
  }

  "BaseController" should "Should return InternalServerError status when code is SERVER_ERROR " in {
    val controller = new BaseController(null, null)
    val result :Result = controller.result("SERVER_ERROR","Error")
    result.header.status should be (500)
  }

}
