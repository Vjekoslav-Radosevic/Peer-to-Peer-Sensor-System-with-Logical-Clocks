# Peer-to-Peer Sensor System with Logical Clocks

This project implements a decentralized distributed system for monitoring sensor readings over time.
It consists of multiple peer-to-peer sensor nodes communicating via UDP, coordinated by Kafka.
The system demonstrates distributed synchronization using **scalar** and **vector** logical clocks, while handling packet loss and retransmission.

---

## Features
- Peer-to-peer network of sensor nodes
- Central coordination of nodes with Kafka (`Start` / `Stop` signals, registration)
- UDP-based communication with simulated packet loss and retransmission
- Emulation of physical clocks for timestamping messages
- Exchange of sensor readings with both scalar and vector clocks
- Periodic sorting of readings by both timestamps
- Computation of average NO₂ values in a 5-second sliding window

---

## Architecture
**Kafka Control Node**
- Publishes control messages via Kafka topics
- Manages node registration through the topics

**Sensor Nodes**
- Emulate sensor readings from a CSV file
- Exchange readings with all other nodes via UDP
- Attach scalar and vector timestamps to each reading
- Send acknowledgments and handle retransmissions on packet loss
- Sort and display readings in order using timestamps
- Compute average values for every 5-second interval

---

## Installation

1. Prerequisites:
	- **Java 21**
	- **Docker & Docker Compose**
2. Clone and build the project:
	```bash
	git clone https://github.com/Vjekoslav-Radosevic/Peer-to-Peer-Sensor-System-with-Logical-Clocks.git
	cd Peer-to-Peer-Sensor-System-with-Logical-Clocks
	gradlew build
	```
3. Run Docker Compose file to start Kafka broker with Raft:
   ```bash
   docker compose up
   ```

---

## Usage

1. **Start the sensor node with:**
	```bash
	gradlew runSensorNode --args="<sensor_port>"
	```

2. **Start the control node with:**
   ```bash
   gradlew runControlNode
   ```
