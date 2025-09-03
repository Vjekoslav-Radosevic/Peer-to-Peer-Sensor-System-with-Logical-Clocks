package hr.fer.repository.impl;

import hr.fer.domain.Reading;
import hr.fer.repository.ReadingDao;

import java.util.*;

public class InMemoryReadingDao implements ReadingDao {

	private final Map<UUID, Reading> readings = new HashMap<>();
	private final Map<UUID, Long> timestamps = new HashMap<>();

	@Override
	public Reading save(Reading reading) {
		UUID id = UUID.randomUUID();
		readings.put(id, reading);
		timestamps.put(id, System.currentTimeMillis());
		return reading;
	}

	@Override
	public List<Reading> findAll() {
		return new ArrayList<>(readings.values());
	}

	@Override
	public List<Reading> findAfterTime(long timestamp) {
		List<Reading> filteredReadings = new ArrayList<>();

		for (Map.Entry<UUID, Long> entry : timestamps.entrySet()) {
			if (entry.getValue() >= timestamp) {
				Reading reading = readings.get(entry.getKey());
				filteredReadings.add(reading);
			}
		}

		return filteredReadings;
	}
}
