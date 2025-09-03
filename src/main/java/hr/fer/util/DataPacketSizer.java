package hr.fer.util;

import hr.fer.domain.Reading;
import hr.fer.infrastructure.model.DataPacket;
import hr.fer.infrastructure.model.PacketType;
import org.apache.commons.lang3.SerializationUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.UUID;

public class DataPacketSizer {

	public static int getMaxDataPacketLength(int peers) {
		long maxNo2 = Long.MAX_VALUE;

		long maxScalarTimestamp = Long.MAX_VALUE;

		HashMap<UUID, Integer> maxVectorTimestamp = new HashMap<>();
		for (int i = 0; i < peers; i++) {
			maxVectorTimestamp.put(UUID.randomUUID(), Integer.MAX_VALUE);
		}

		Reading maxReading = new Reading(maxNo2, maxScalarTimestamp, maxVectorTimestamp);

		InetSocketAddress maxInetSocketAddress = new InetSocketAddress("localhost", 65535);

		DataPacket maxDataPacket = new DataPacket(UUID.randomUUID(), PacketType.DATA, maxReading, maxInetSocketAddress);

		byte[] serializedPacket = SerializationUtils.serialize(maxDataPacket);

		return serializedPacket.length;
	}
}
