package org.sunbird.viewer.core

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

import java.util.HashMap
import java.util.concurrent.Future


class KafkaUtil {

  val props = new HashMap[String, Object]()
  props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000L.asInstanceOf[Object])
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getString("kafka.broker.list"))
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  private var producer: KafkaProducer[String, String] = _

  def send(event: String, topic: String): Future[RecordMetadata] = {
    if(null == producer) producer = new KafkaProducer[String, String](props)
    val message = new ProducerRecord[String, String](topic, null, event)
    producer.send(message);
  }
  
  def close() {
    if(null != producer)
      producer.close();
  }

  def checKConnection():Boolean = {
    try {
      val consumer = new KafkaConsumer[String, String](props,new StringDeserializer,new StringDeserializer)
      consumer.listTopics()
      consumer.close();
      true
    }catch {
      case ex: Exception => throw new Exception("Kafka :" + ex.getMessage)
      false
    }
  }
}