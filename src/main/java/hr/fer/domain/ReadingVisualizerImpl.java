package hr.fer.domain;

import hr.fer.repository.ReadingDao;

import java.util.*;
import java.util.stream.Collectors;

public class ReadingVisualizerImpl implements ReadingVisualizer {

	private final ReadingDao readingDao;
	private final ReadingLogger readingLogger;

	public ReadingVisualizerImpl(ReadingDao readingDao, ReadingLogger readingLogger) {
		this.readingDao = readingDao;
		this.readingLogger = readingLogger;
	}

	@Override
	public void visualizeReadings() {
		long timestampBefore5s = System.currentTimeMillis() - 5000;
		List<Reading> readings = readingDao.findAfterTime(timestampBefore5s);

		double averageReading = calculateAverageReading(readings);

		List<Reading> readingsSortedByScalar = sortByScalarTime(readings);
		List<Reading> readingsSortedByVector = sortByVectorTime(readings);

		readingLogger.log(averageReading, readingsSortedByScalar, readingsSortedByVector);
	}

	private double calculateAverageReading(List<Reading> readings) {
		return readings.stream()
			.mapToDouble(Reading::no2)
			.average()
			.orElse(0.0);
	}

	private List<Reading> sortByScalarTime(List<Reading> readings) {
		return readings.stream()
			.sorted(Comparator.comparingLong(Reading::scalarTimestamp))
			.collect(Collectors.toList());
	}

	private List<Reading> sortByVectorTime(List<Reading> readings) {
		List<Reading> sortedReadings = new ArrayList<>();
		List<Reading> unsortedReadings = new ArrayList<>(readings);

		while (!unsortedReadings.isEmpty()) {
			Reading greatest = findGreatest(unsortedReadings);
			unsortedReadings.remove(greatest);
			sortedReadings.addFirst(greatest);
		}

		return sortedReadings;
	}

	private Reading findGreatest(List<Reading> readings) {
		Reading greatest = readings.getFirst();

		for (Reading reading : readings) {
			if (compareVectors(reading.vectorTimestamp(), greatest.vectorTimestamp()) > 0) {
				greatest = reading;
			}
		}

		return greatest;
	}

	private int compareVectors(Map<UUID, Integer> vector1, Map<UUID, Integer> vector2) {
		boolean hasLower = false;
		boolean hasGreater = false;

		for (UUID id : vector1.keySet()) {
			if (vector1.get(id) < vector2.get(id)) {
				hasLower = true;
			} else if (vector1.get(id) > vector2.get(id)) {
				hasGreater = true;
			}
		}

		if (hasLower && !hasGreater) {
			return -1;
		} else if (hasGreater && !hasLower) {
			return 1;
		} else {
			return 0; // Concurrent vectors
		}
	}
}
