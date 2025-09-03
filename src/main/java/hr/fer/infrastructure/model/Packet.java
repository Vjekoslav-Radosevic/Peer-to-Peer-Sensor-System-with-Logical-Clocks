package hr.fer.infrastructure.model;

import java.io.Serializable;
import java.util.UUID;

public class Packet implements Serializable {
	private final UUID id;
	private final PacketType type;

	public Packet(UUID id, PacketType type) {
		this.id = id;
		this.type = type;
	}

	public UUID getId() {
		return id;
	}

	public PacketType getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("Packet { id=%s }", getId());
	}
}
