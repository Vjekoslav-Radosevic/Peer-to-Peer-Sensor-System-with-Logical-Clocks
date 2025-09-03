package hr.fer.infrastructure.udp;

import hr.fer.domain.ReadingManager;
import hr.fer.infrastructure.model.DataPacket;
import hr.fer.infrastructure.model.Packet;
import hr.fer.infrastructure.model.PacketType;
import hr.fer.repository.MessageDao;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpReadingReceiver implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(UdpReadingReceiver.class);

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final int receiverBufferLength;

	private final SimpleSimulatedDatagramSocket socket;
	private final ReadingManager readingManager;
	private final MessageDao messageDao;
	private final Set<UUID> receivedPacketIds = new HashSet<>();

	public UdpReadingReceiver(SimpleSimulatedDatagramSocket socket, int receiverBufferLength, ReadingManager readingManager, MessageDao messageDao) {
		this.socket = socket;
		this.receiverBufferLength = receiverBufferLength;
		this.readingManager = readingManager;
		this.messageDao = messageDao;
	}

	public void listen() {
		byte[] rcvBuff = new byte[receiverBufferLength];

		DatagramPacket recvDatagramPacket = new DatagramPacket(rcvBuff, rcvBuff.length);

		try {
			socket.receive(recvDatagramPacket);
			Packet recvPacket = SerializationUtils.deserialize(recvDatagramPacket.getData());
			PacketType packetType = recvPacket.getType();

			if (packetType != PacketType.DATA && packetType != PacketType.ACK) {
				logger.info("[UDP Receiver] Received packet that is not of type 'data' or 'ack': {}", recvPacket);
				return;
			}

			if (packetType == PacketType.ACK) {
				messageDao.deleteByPacketId(recvPacket.getId());
				logger.info("[UDP Receiver] Received ACK for packet with id {}", recvPacket.getId());
				return;
			}

			DataPacket recvDataPacket = (DataPacket) recvPacket;

			if (!receivedPacketIds.contains(recvDataPacket.getId())) {
				logger.info("[UDP Receiver] Received NEW {}", recvDataPacket);
				receivedPacketIds.add(recvDataPacket.getId());
				readingManager.manageNewRemoteReading(recvDataPacket.getReading());
			} else {
				logger.info("[UDP Receiver] Already received {}", recvDataPacket);
			}

			sendAck(recvDataPacket);

		} catch (SocketTimeoutException ignored) {
		} catch (IOException ex) {
			logger.error("[UDP Receiver] Error while receiving packet: {}", ex.getMessage());
			running.set(false);
		}
	}

	private void sendAck(DataPacket packet) {
		Packet sendPacket = new Packet(packet.getId(), PacketType.ACK);
		byte[] sendBuff = SerializationUtils.serialize(sendPacket);
		DatagramPacket sendDatagramPacket = new DatagramPacket(sendBuff, sendBuff.length, packet.getResponseAddress());

		try {
			socket.send(sendDatagramPacket);
		} catch (IOException e) {
			logger.error("[UDP Receiver] Could not send ACK to {}", packet.getResponseAddress());
		}
	}

	public void close() {
		if (running.get()) {
			running.set(false);
		} else { // thread not even started
			cleanUp();
		}
	}

	public void cleanUp() {
		if (socket != null && !socket.isClosed()) {
			socket.close();
			logger.info("[UDP Receiver] Receiver closed");
		}
	}

	@Override
	public void run() {
		logger.info("[UDP Receiver] Receiver started - ready to receive remote readings");

		running.set(true);

		try {
			while (running.get()) listen();
		} catch (Exception e) {
			logger.error("[UDP Receiver] Unexpected error occurred", e);
		} finally {
			cleanUp();
		}
	}
}
