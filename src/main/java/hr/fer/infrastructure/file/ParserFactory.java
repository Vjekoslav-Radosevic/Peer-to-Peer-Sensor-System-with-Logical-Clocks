package hr.fer.infrastructure.file;

import hr.fer.domain.ReadingManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ParserFactory {

	public static CsvParser createCsvParser(String filename, ReadingManager readingManager) throws URISyntaxException, IOException {

		URL url = ParserFactory.class.getClassLoader().getResource(filename);

		if (url != null) {
			Path path = Paths.get(url.toURI());
			return new CsvParser(Files.readAllLines(path), readingManager);
		} else {
			throw new FileNotFoundException(String.format("File '%s' not found", filename));
		}
	}
}
