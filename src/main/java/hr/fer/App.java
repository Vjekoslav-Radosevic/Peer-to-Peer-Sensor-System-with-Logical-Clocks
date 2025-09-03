package hr.fer;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.domain.*;
import hr.fer.infrastructure.console.ConsoleReadingLogger;
import hr.fer.infrastructure.file.CsvParser;
import hr.fer.infrastructure.file.ParserFactory;
import hr.fer.infrastructure.kafka.KafkaFactory;
import hr.fer.infrastructure.kafka.KafkaSensorPublisher;
import hr.fer.infrastructure.kafka.KafkaSensorSubscriber;
import hr.fer.infrastructure.model.Sensor;
import hr.fer.infrastructure.udp.SimpleSimulatedDatagramSocket;
import hr.fer.infrastructure.udp.UdpReadingReceiver;
import hr.fer.infrastructure.udp.UdpReadingSender;
import hr.fer.repository.MessageDao;
import hr.fer.repository.ReadingDao;
import hr.fer.repository.SensorDao;
import hr.fer.repository.impl.InMemoryMessageDao;
import hr.fer.repository.impl.InMemoryReadingDao;
import hr.fer.repository.impl.InMemorySensorDao;
import hr.fer.util.DataPacketSizer;
import hr.fer.util.EmulatedSystemClock;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private static final String IP = "localhost";
	private static final double LOSS_RATE = 0.3;
	private static final int AVERAGE_DELAY = 1000;
	private static final String READINGS_FILENAME = "readings.csv";
	private static final String KAFKA_BROKER_IP = "localhost";
	private static final int KAFKA_BROKER_PORT = 9092;

	private UdpReadingSender udpReadingSender;
	private UdpReadingReceiver udpReadingReceiver;
	private KafkaSensorPublisher kafkaSensorPublisher;
	private KafkaSensorSubscriber kafkaSensorSubscriber;
	private ScheduledExecutorService readingScheduler;

	private Thread udpReceiverThread;
	private Thread kafkaSubscriberThread;

	public static void main(String[] args) throws InterruptedException {
		if (args.length != 1) {
			System.out.println("Usage: .\\gradlew runSensorNode --args=\"<sensor_port>\"");
			return;
		}

		int port = Integer.parseInt(args[0]);
		App app = new App();

		try {
			app.run(port);
		} catch (Exception e) {
			logger.error("[App] Unexpected error occurred", e);
		} finally {
			app.cleanUp();
		}
	}

	private void run(int port) throws InterruptedException {
		Sensor sensor = new Sensor(UUID.randomUUID(), new InetSocketAddress(IP, port));

		SensorDao sensorDao = new InMemorySensorDao(sensor);
		ReadingDao readingDao = new InMemoryReadingDao();
		MessageDao messageDao = new InMemoryMessageDao();

		ObjectMapper objectMapper = new ObjectMapper();

		SimpleSimulatedDatagramSocket senderSocket = null;
		try {
			senderSocket = new SimpleSimulatedDatagramSocket(LOSS_RATE, AVERAGE_DELAY);
		} catch (SocketException e) {
			logger.error("[App] Could not create UDP client socket", e);
			System.exit(1);
		}

		udpReadingSender = new UdpReadingSender(senderSocket, sensorDao, messageDao);
		ReadingLogger readingLogger = new ConsoleReadingLogger();
		EmulatedSystemClock emulatedSystemClock = new EmulatedSystemClock();

		ReadingManager readingManager = new ReadingManagerImpl(udpReadingSender, emulatedSystemClock, sensorDao, readingDao);
		ReadingVisualizer readingVisualizer = new ReadingVisualizerImpl(readingDao, readingLogger);

		CsvParser csvParser = null;
		try {
			csvParser = ParserFactory.createCsvParser(READINGS_FILENAME, readingManager);
		} catch (URISyntaxException e) {
			logger.error("[App] Filename '{}' provided to ParserFactory is invalid -> stopping program", READINGS_FILENAME);
			cleanUp();
			System.exit(1);
		} catch (IOException e) {
			logger.error("[App] Could not read the contents of '{}' -> stopping program", READINGS_FILENAME);
			cleanUp();
			System.exit(1);
		}

		CountDownLatch startSignal = new CountDownLatch(1);

		Producer<String, String> producer = KafkaFactory.createProducer(String.format("%s:%s", KAFKA_BROKER_IP, KAFKA_BROKER_PORT));
		Consumer<String, String> consumer = KafkaFactory.createConsumer(String.valueOf(sensor.id()), String.format("%s:%s", KAFKA_BROKER_IP, KAFKA_BROKER_PORT));

		kafkaSensorPublisher = new KafkaSensorPublisher(producer, objectMapper);

		kafkaSensorSubscriber = new KafkaSensorSubscriber(consumer, objectMapper, sensorDao, command -> {
			if ("start".equals(command)) {
				startSignal.countDown();
			}
		});

		kafkaSubscriberThread = new Thread(kafkaSensorSubscriber, "Kafka-Sensor-Subscriber");
		kafkaSubscriberThread.start();

		startSignal.await();

		kafkaSensorPublisher.registerSensor(sensor);

		Thread.sleep(5000); // wait for all nodes to become aware of each other

		SimpleSimulatedDatagramSocket receiverSocket = null;
		try {
			receiverSocket = new SimpleSimulatedDatagramSocket(sensor.address().getPort(), LOSS_RATE, AVERAGE_DELAY);
		} catch (SocketException e) {
			logger.error("[App] Could not create UDP server socket", e);
			cleanUp();
			System.exit(1);
		}

		int receiveBufferLength = DataPacketSizer.getMaxDataPacketLength(sensorDao.findAll().size());

		udpReadingReceiver = new UdpReadingReceiver(receiverSocket, receiveBufferLength, readingManager, messageDao);
		udpReceiverThread = new Thread(udpReadingReceiver, "UDP-Reading-Receiver");
		udpReceiverThread.start();

		readingManager.initTimestamps();

		readingScheduler = Executors.newSingleThreadScheduledExecutor();
		CsvParser finalCsvParser = csvParser;
		readingScheduler.scheduleAtFixedRate(() -> {
			finalCsvParser.parseNewReading();
			readingVisualizer.visualizeReadings();
		}, 0, 5, TimeUnit.SECONDS);

		kafkaSubscriberThread.join();
	}

	private void cleanUp() {
		try {
			if (udpReadingReceiver != null) udpReadingReceiver.close();
			if (udpReadingSender != null) udpReadingSender.close();

			if (readingScheduler != null) readingScheduler.shutdown();

			if (kafkaSensorPublisher != null) kafkaSensorPublisher.close();
			if (kafkaSensorSubscriber != null) kafkaSensorSubscriber.close();

			if (udpReceiverThread != null) udpReceiverThread.join(5000);
			if (kafkaSubscriberThread != null) kafkaSubscriberThread.join(5000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Cleanup interrupted", e);
		}
	}
}
