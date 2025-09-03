package hr.fer.infrastructure.file;

import hr.fer.domain.ReadingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CsvParser {

	private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);

	private final List<String> rows;
	private final long startTimeMillis;
	private final ReadingManager readingManager;

	public CsvParser(List<String> rows, ReadingManager readingManager) {
		this.rows = rows;
		this.readingManager = readingManager;
		this.startTimeMillis = System.currentTimeMillis();
	}

	public void parseNewReading() {
		long elapsedTime = (System.currentTimeMillis() - startTimeMillis) / 1000;

		int index = (int) ((elapsedTime % 100) + 1);

		String row = rows.get(index);

		String[] parts = row.split(",", -1);

		double no2 = parts[4].isEmpty() ? 0.0 : Double.parseDouble(parts[4]);

		readingManager.manageNewLocalReading(no2);
	}
}
