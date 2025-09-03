package hr.fer.infrastructure.model;

import hr.fer.domain.Reading;

import java.net.InetSocketAddress;
import java.util.UUID;

public class DataPacket extends Packet {

	private final Reading reading;
	private final InetSocketAddress responseAddress;

	public DataPacket(UUID id, PacketType type, Reading reading, InetSocketAddress responseAddress) {
		super(id, type);
		this.reading = reading;
		this.responseAddress = responseAddress;
	}

	public Reading getReading() {
		return reading;
	}

	public InetSocketAddress getResponseAddress() {
		return responseAddress;
	}

	@Override
	public String toString() {
		return String.format("DataPacket { id=%s, %s }", getId(), getReading());
	}
}
