package hr.fer.repository.impl;

import hr.fer.infrastructure.model.Message;
import hr.fer.repository.MessageDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMessageDao implements MessageDao {

	private final Map<UUID, Message> messages = new ConcurrentHashMap<>();

	@Override
	public Message save(Message message) {
		messages.put(message.id(), message);
		return message;
	}

	@Override
	public List<Message> findAll() {
		return new ArrayList<>(messages.values());
	}

	@Override
	public void deleteByPacketId(UUID id) {
		messages.entrySet().removeIf((entry) ->
			entry.getValue().packet().getId().equals(id)
		);
	}
}
