package org.sunbird.viewer.platform

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

import java.time.Duration

class RedisUtil {

  private val redis_host = AppConfig.getString("redis.host")
  private val redis_port = AppConfig.getInt("redis.port")

  private def buildPoolConfig = {
    val poolConfig = new JedisPoolConfig
    poolConfig.setMaxTotal(AppConfig.getInt("redis.connection.max"))
    poolConfig.setMaxIdle(AppConfig.getInt("redis.connection.idle.max"))
    poolConfig.setMinIdle(AppConfig.getInt("redis.connection.idle.min"))
    poolConfig.setTestWhileIdle(true)
    poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(AppConfig.getInt("redis.connection.minEvictableIdleTimeSeconds")).toMillis)
    poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(AppConfig.getInt("redis.connection.timeBetweenEvictionRunsSeconds")).toMillis)
    poolConfig.setBlockWhenExhausted(true)
    poolConfig
  }

  protected var jedisPool: JedisPool = new JedisPool(buildPoolConfig, redis_host, redis_port)

  def getConnection(database: Int): Jedis = {
    val conn = jedisPool.getResource
    conn.select(database)
    conn
  }

  def resetConnection(): Unit = {
    jedisPool.close()
    jedisPool = new JedisPool(buildPoolConfig, redis_host, redis_port)
  }

  def closePool() = {
    jedisPool.close()
  }

  def checkConnection = {
    try {
      val conn = getConnection(2)
      conn.close()
      true;
    } catch {
      case ex: Exception => throw  new Exception("Redis:" + ex.getMessage)
    }
  }
}
