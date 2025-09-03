package hr.fer.domain;

import hr.fer.infrastructure.model.Sensor;
import hr.fer.repository.ReadingDao;
import hr.fer.repository.SensorDao;
import hr.fer.util.EmulatedSystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ReadingManagerImpl implements ReadingManager {

	private static final Logger logger = LoggerFactory.getLogger(ReadingManagerImpl.class);

	private final ReadingSender readingSender;
	private final EmulatedSystemClock emulatedSystemClock;

	private final SensorDao sensorDao;
	private final ReadingDao readingDao;

	private long scalarTimestamp;
	private final HashMap<UUID, Integer> vectorTimestamp = new HashMap<>();

	public ReadingManagerImpl(ReadingSender readingSender, EmulatedSystemClock emulatedSystemClock, SensorDao sensorDao, ReadingDao readingDao) {
		this.readingSender = readingSender;
		this.emulatedSystemClock = emulatedSystemClock;

		this.sensorDao = sensorDao;
		this.readingDao = readingDao;
	}

	public void initTimestamps() {
		scalarTimestamp = emulatedSystemClock.currentTimeMillis();

		List<Sensor> sensors = sensorDao.findAll();
		for (Sensor s : sensors) {
			vectorTimestamp.put(s.id(), 0);
		}
	}

	@Override
	public void manageNewLocalReading(double no2) {
		Sensor localSensor = sensorDao.findLocal();

		// update timestamps
		vectorTimestamp.merge(localSensor.id(), 1, Integer::sum);
		scalarTimestamp = Math.max(emulatedSystemClock.currentTimeMillis(), scalarTimestamp + 1);

		Reading reading = new Reading(no2, scalarTimestamp, new HashMap<>(vectorTimestamp));

		logger.info("[Reading Manager] Got new LOCAL {}", reading);

		readingDao.save(reading);

		readingSender.sendReadingToPeers(reading);
	}

	@Override
	public void manageNewRemoteReading(Reading reading) {
		logger.info("[Reading Manager] Got new REMOTE {}", reading);

		Sensor localSensor = sensorDao.findLocal();

		// update timestamps because of receiving reading
		vectorTimestamp.merge(localSensor.id(), 1, Integer::sum);
		scalarTimestamp = Math.max(emulatedSystemClock.currentTimeMillis(), scalarTimestamp + 1);

		// update scalar time based on remote reading
		long remoteScalarTimestamp = reading.scalarTimestamp() + 1;
		if (reading.scalarTimestamp() > scalarTimestamp) {
			logger.info("[Reading Manager] Remote scalar time ({}) greater than local ({}) -> updating from {} to {}", remoteScalarTimestamp, scalarTimestamp, scalarTimestamp, remoteScalarTimestamp);
			scalarTimestamp = remoteScalarTimestamp;
		} else {
			logger.info("[Reading Manager] Local scalar time ({}) greater than remote ({}) -> ignoring remote scalar time", scalarTimestamp, remoteScalarTimestamp);
		}

		// update vector time based on remote reading
		HashMap<UUID, Integer> oldVectorTimestamp = new HashMap<>(vectorTimestamp);
		HashMap<UUID, Integer> remoteVectorTimestamp = new HashMap<>(reading.vectorTimestamp());

		for (UUID id : remoteVectorTimestamp.keySet()) {
			if (id.equals(localSensor.id())) continue;

			if (vectorTimestamp.get(id) < remoteVectorTimestamp.get(id)) {
				vectorTimestamp.put(id, remoteVectorTimestamp.get(id));
			}
		}

		logger.info("[Reading Manager] Updating vector time from {} to {}", oldVectorTimestamp, vectorTimestamp);

		readingDao.save(reading);
	}
}
