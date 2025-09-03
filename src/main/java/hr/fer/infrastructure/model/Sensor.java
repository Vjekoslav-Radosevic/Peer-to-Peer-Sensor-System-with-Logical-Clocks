package hr.fer.infrastructure.model;

import java.net.InetSocketAddress;
import java.util.UUID;

public record Sensor(UUID id, InetSocketAddress address) {

	@Override
	public String toString() {
		return String.format("Sensor { id=%s, address=%s }", id, address);
	}
}
