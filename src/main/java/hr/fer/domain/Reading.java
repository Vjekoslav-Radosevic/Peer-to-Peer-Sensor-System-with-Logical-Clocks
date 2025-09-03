package hr.fer.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public record Reading(double no2, long scalarTimestamp, HashMap<UUID, Integer> vectorTimestamp) implements Serializable {

	@Override
	public String toString() {
		return String.format("Reading { no2=%s, scalar=%s, vector=%s }", no2, scalarTimestamp, vectorTimestamp);
	}
}
