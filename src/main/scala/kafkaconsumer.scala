
  import java.util.{Collections, Properties}
  import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
  import org.apache.kafka.common.serialization.StringDeserializer

  object KafkaConsumer extends App {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList("DogAPITopic"))

    while (true) {
      val records = consumer.poll(100)
      records.forEach { record =>
        println(s"Consumed message: ${record.key()} -> ${record.value()}")
      }
    }
  }


