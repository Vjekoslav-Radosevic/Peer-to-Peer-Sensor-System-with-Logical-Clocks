package hr.fer.domain;

import hr.fer.infrastructure.model.Sensor;

public interface SensorPublisher {

	void registerSensor(Sensor sensor);

	void startSystem();

	void stopSystem();
}
