package hr.fer.infrastructure.model;

import java.net.InetSocketAddress;
import java.util.UUID;

public record Message(UUID id, InetSocketAddress address, Packet packet) {

	@Override
	public String toString() {
		return String.format("Message { id=%s, address=%s, %s}", id, address, packet);
	}
}
