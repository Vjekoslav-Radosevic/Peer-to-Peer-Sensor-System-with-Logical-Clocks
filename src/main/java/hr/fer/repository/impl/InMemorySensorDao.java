package hr.fer.repository.impl;

import hr.fer.infrastructure.model.Sensor;
import hr.fer.repository.SensorDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySensorDao implements SensorDao {

	private final Map<UUID, Sensor> sensors = new ConcurrentHashMap<>();
	private final Sensor localSensor;

	public InMemorySensorDao(Sensor localSensor) {
		this.localSensor = localSensor;
	}

	@Override
	public Sensor save(Sensor sensor) {
		sensors.put(sensor.id(), sensor);
		return sensor;
	}

	@Override
	public List<Sensor> findAll() {
		return new ArrayList<>(sensors.values());
	}

	@Override
	public Sensor findLocal() {
		return localSensor;
	}

	@Override
	public List<Sensor> findRemote() {
		List<Sensor> allSensors = new ArrayList<>(sensors.values());

		return allSensors
			.stream()
			.filter((sensor) -> !sensor.id().equals(localSensor.id()))
			.toList();
	}
}
