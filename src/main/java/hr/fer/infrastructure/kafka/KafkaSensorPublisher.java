package hr.fer.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.domain.SensorPublisher;
import hr.fer.infrastructure.model.Sensor;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaSensorPublisher implements SensorPublisher {

	private final String REGISTER_TOPIC = "register";
	private final String COMMAND_TOPIC = "command";

	private final String START_MESSAGE = "start";
	private final String STOP_MESSAGE = "stop";

	private final Producer<String, String> producer;
	private final ObjectMapper objectMapper;

	private static final Logger logger = LoggerFactory.getLogger(KafkaSensorPublisher.class);

	public KafkaSensorPublisher(Producer<String, String> producer, ObjectMapper objectMapper) {
		this.producer = producer;
		this.objectMapper = objectMapper;
	}

	@Override
	public void registerSensor(Sensor sensor) {
		try {
			String data = objectMapper.writeValueAsString(sensor);

			ProducerRecord<String, String> record = new ProducerRecord<>(REGISTER_TOPIC, null, data);

			producer.send(record, (metadata, exception) -> {
				if (exception == null) {
					logger.info("[Kafka Publisher] Sending message '{}' to topic '{}'", data, REGISTER_TOPIC);
				} else {
					logger.error("[Kafka Publisher] Error sending message: {}", exception.getMessage());
				}
			});
			producer.flush();
		} catch (JsonProcessingException e) {
			logger.error("[Kafka Publisher] Could not serialize Sensor data");
		}
	}

	@Override
	public void startSystem() {
		ProducerRecord<String, String> record = new ProducerRecord<>(COMMAND_TOPIC, null, START_MESSAGE);

		producer.send(record, (metadata, exception) -> {
			if (exception == null) {
				logger.info("[Kafka Publisher] Message '{}' sent to topic '{}'", START_MESSAGE, REGISTER_TOPIC);
			} else {
				logger.error("[Kafka Publisher] Error sending message: {}", exception.getMessage());
			}
		});
		producer.flush();
	}

	@Override
	public void stopSystem() {
		ProducerRecord<String, String> record = new ProducerRecord<>(COMMAND_TOPIC, null, "stop");

		producer.send(record, (metadata, exception) -> {
			if (exception == null) {
				logger.info("[Kafka Publisher] Message '{}' sent to topic '{}'", STOP_MESSAGE, REGISTER_TOPIC);
			} else {
				logger.error("[Kafka Publisher] Error sending message: {}", exception.getMessage());
			}
		});
		producer.flush();
	}

	public void close() {
		if (producer != null) {
			producer.close();
			logger.info("[Kafka Publisher] Publisher closed");
		}
	}
}
