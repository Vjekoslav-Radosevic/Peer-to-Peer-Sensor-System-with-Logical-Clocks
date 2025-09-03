package hr.fer.infrastructure.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaFactory {

	public static Consumer<String, String> createConsumer(String groupId, String bootstrapServers) {
		Properties properties = new Properties();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		return new KafkaConsumer<>(properties);
	}

	public static Producer<String, String> createProducer(String bootstrapServers) {
		Properties properties = new Properties();

		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		properties.put("acks", "1");

		return new KafkaProducer<>(properties);
	}
}
