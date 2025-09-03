package hr.fer.infrastructure.console;

import hr.fer.domain.Reading;
import hr.fer.domain.ReadingLogger;

import java.util.List;

public class ConsoleReadingLogger implements ReadingLogger {

	@Override
	public void log(double avg, List<Reading> readingsSortedByScalar, List<Reading> readingsSortedByVector) {
		StringBuilder sb = new StringBuilder();
		sb.append("======================== Printing readings ========================\n");
		sb.append("Average reading: ").append(avg).append("\n");

		sb.append("\nSorted by scalar time:\n");
		for (Reading reading : readingsSortedByScalar) {
			sb.append(reading).append("\n");
		}

		sb.append("\nSorted by vector time:\n");
		for (Reading reading : readingsSortedByVector) {
			sb.append(reading).append("\n");
		}

		sb.append("===================================================================\n");
		System.out.print(sb);
	}
}
