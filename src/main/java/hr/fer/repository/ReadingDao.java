package hr.fer.repository;

import hr.fer.domain.Reading;

import java.util.List;

public interface ReadingDao {

	Reading save(Reading reading);

	List<Reading> findAll();

	List<Reading> findAfterTime(long timestamp);
}
