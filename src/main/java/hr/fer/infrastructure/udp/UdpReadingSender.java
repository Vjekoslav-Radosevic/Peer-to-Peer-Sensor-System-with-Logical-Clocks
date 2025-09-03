package hr.fer.infrastructure.udp;

import hr.fer.domain.Reading;
import hr.fer.domain.ReadingSender;
import hr.fer.infrastructure.model.*;
import hr.fer.repository.MessageDao;
import hr.fer.repository.SensorDao;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;
import java.util.UUID;

public class UdpReadingSender implements ReadingSender {

	private static final Logger logger = LoggerFactory.getLogger(UdpReadingSender.class);

	private final SimpleSimulatedDatagramSocket socket;

	private final SensorDao sensorDao;
	private final MessageDao messageDao;

	public UdpReadingSender(SimpleSimulatedDatagramSocket socket, SensorDao sensorDao, MessageDao messageDao) {
		this.socket = socket;
		this.sensorDao = sensorDao;
		this.messageDao = messageDao;
	}

	@Override
	public void sendReadingToPeers(Reading reading) {
		// send all pending messages first
		List<Message> messages = messageDao.findAll();
		for (Message message : messages) {
			sendMessage(message);
		}

		// send new reading to all peers
		Sensor localSensor = sensorDao.findLocal();
		List<Sensor> peers = sensorDao.findRemote();
		for (Sensor peer : peers) {
			Packet packet = new DataPacket(UUID.randomUUID(), PacketType.DATA, reading, localSensor.address());
			Message message = new Message(UUID.randomUUID(), peer.address(), packet);
			sendMessage(message);
			messageDao.save(message);
		}

	}

	private void sendMessage(Message message) {
		byte[] sendBuff = SerializationUtils.serialize(message.packet());
		DatagramPacket sendDatagramPacket = new DatagramPacket(sendBuff, sendBuff.length, message.address());

		logger.info("[UDP Sender] Sending {} to {}", message.packet(), message.address());

		try {
			this.socket.send(sendDatagramPacket);
		} catch (IOException ex) {
			logger.info("[UDP Sender] Error while sending {} to {}", message.packet(), message.address());
		}
	}

	public void close() {
		if (socket != null && !socket.isClosed()) {
			socket.close();
			logger.info("[UDP Sender] Sender closed");
		}
	}
}
