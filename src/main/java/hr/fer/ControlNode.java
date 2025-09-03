package hr.fer;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.infrastructure.kafka.KafkaFactory;
import hr.fer.infrastructure.kafka.KafkaSensorPublisher;
import org.apache.kafka.clients.producer.Producer;

import java.util.Scanner;

public class ControlNode {

	public static void main(String[] args) {

		Producer<String, String> producer = KafkaFactory.createProducer("localhost:9092");

		ObjectMapper objectMapper = new ObjectMapper();

		KafkaSensorPublisher kafkaPublisher = new KafkaSensorPublisher(producer, objectMapper);

		Scanner scanner = new Scanner(System.in);

		boolean exit = false;

		while (!exit) {
			System.out.print("Type start|stop to send start|stop message to topic 'command' (type exit to exit program): ");

			String inputText = scanner.nextLine();

			switch (inputText) {
				case "start" -> kafkaPublisher.startSystem();
				case "stop" -> kafkaPublisher.stopSystem();
				case "exit" -> exit = true;
				default -> System.out.println("Command not recognised");
			}
		}

		kafkaPublisher.close();
	}

}
