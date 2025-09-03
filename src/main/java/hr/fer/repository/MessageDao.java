package hr.fer.repository;

import hr.fer.infrastructure.model.Message;

import java.util.List;
import java.util.UUID;

public interface MessageDao {

	Message save(Message message);

	List<Message> findAll();

	void deleteByPacketId(UUID id);
}
