package hr.fer.domain;

public interface ReadingManager {

	void initTimestamps();

	void manageNewLocalReading(double no2);

	void manageNewRemoteReading(Reading reading);
}
