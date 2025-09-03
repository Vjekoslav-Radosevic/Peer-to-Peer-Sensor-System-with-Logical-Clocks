package hr.fer.domain;

import java.util.List;

public interface ReadingLogger {
	void log(double avg, List<Reading> readingsSortedByScalar, List<Reading> readingsSortedByVector);
}
