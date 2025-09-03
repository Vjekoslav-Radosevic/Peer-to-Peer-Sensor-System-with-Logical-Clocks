package hr.fer.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.infrastructure.model.Sensor;
import hr.fer.repository.SensorDao;
import hr.fer.util.CommandListener;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaSensorSubscriber implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(KafkaSensorSubscriber.class);

	private final String REGISTER_TOPIC = "register";
	private final String COMMAND_TOPIC = "command";

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final Consumer<String, String> consumer;
	private final ObjectMapper objectMapper;
	private final SensorDao sensorDao;
	private final CommandListener listener;

	public KafkaSensorSubscriber(Consumer<String, String> consumer, ObjectMapper objectMapper, SensorDao sensorDao, CommandListener listener) {
		this.consumer = consumer;
		this.objectMapper = objectMapper;
		this.sensorDao = sensorDao;
		this.listener = listener;
	}

	private void listen() {
		ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

		records.forEach(record -> {
			if (COMMAND_TOPIC.equals(record.topic()) && "stop".equals(record.value())) {
				logger.info("[Kafka subscriber] Received message 'stop' from topic 'command' -> shutting down");
				running.set(false);

			} else if (COMMAND_TOPIC.equals(record.topic()) && "start".equals(record.value())) {
				logger.info("[Kafka Subscriber] Received message 'start' from topic 'command' -> starting system");
				listener.onCommand("start");

			} else if (REGISTER_TOPIC.equals(record.topic())) {
				try {
					Sensor sensor = objectMapper.readValue(record.value(), Sensor.class);
					sensorDao.save(sensor);
					logger.info("[Kafka Subscriber] Added new {}", sensor);

				} catch (JsonProcessingException e) {
					logger.error("[Kafka Subscriber] Failed to parse Sensor JSON: ", e);
				}

			} else {
				logger.info("[Kafka Subscriber] Ignoring message '{}' from topic '{}'", record.value(), record.topic());
			}
		});
		consumer.commitSync();
	}

	public void close() {
		if (running.get()) {
			this.running.set(false);
		} else { // thread not even started
			cleanUp();
		}
	}

	public void cleanUp() {
		if (consumer != null) {
			consumer.close();
			logger.info("[Kafka Subscriber] Subscriber closed");
		}
	}

	@Override
	public void run() {
		this.consumer.subscribe(Arrays.asList(REGISTER_TOPIC, COMMAND_TOPIC));
		logger.info("[Kafka Subscriber] Subscriber started - subscribed to topics: {}", consumer.subscription());

		running.set(true);

		try {
			while (running.get()) listen();
		} catch (Exception e) {
			logger.error("[Kafka Subscriber] Unexpected error occurred", e);
		} finally {
			cleanUp();
		}
	}
}
