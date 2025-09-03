package hr.fer.repository;

import hr.fer.infrastructure.model.Sensor;

import java.util.List;

public interface SensorDao {

	Sensor save(Sensor sensor);

	List<Sensor> findAll();

	Sensor findLocal();

	List<Sensor> findRemote();
}
