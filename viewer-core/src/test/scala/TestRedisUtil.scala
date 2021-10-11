import org.scalatest.{FlatSpec, Matchers}
import org.sunbird.viewer.core.RedisUtil
import redis.embedded.RedisServer

class TestRedisUtil extends FlatSpec  with Matchers  {

  private val redisServer: RedisServer =new RedisServer(6368)
  val redisUtil = new RedisUtil();


  "RedisUtil" should "test the connection" in {
    redisServer.stop()
    intercept[Exception] {
      redisUtil.checkConnection should be(false)
    }
  }

  "RedisUtil" should "test the reset connection" in {
    redisServer.start()
    redisUtil.resetConnection()
    redisUtil.checkConnection should be(true)
    redisServer.stop()
  }
}
