# Clustered Smart Home Alarm System

## Overview

Extend the Smart Home Alarm System from the previous assignment so that it runs on an Apache Pekko Cluster.
The alarm logic remains the same, but sensors, keypads, and alarm control units may now run on different cluster nodes. 

---

## Functional Requirements

### 1. System States

State transitions must follow the same rules as in the previous assignment.

---

### 2. Cluster Architecture

The system must run on multiple Apache Pekko Cluster nodes.

Mandatory constraints:

* At least three cluster nodes must be supported
* Sensors, keypads, and alarm control actors may run on different nodes
* Communication must happen only through actor messages

---

### 3. Sensor Handling

Sensors must work as distributed actors.

Mandatory constraints:

* Each sensor has a `sensorId`
* The system must support motion sensors and door/window sensors
* Sensor events must be sent to the alarm control entity 

---

### 4. Timing Constraints

The system must keep the same timing behavior as the previous assignment.

---

### 5. Failure and Recovery

If an alarm control entity is restarted or recreated, its previous state is lost.

Mandatory recovery behavior:

* The entity must enter a safe recovery mode
* It must not assume the system is disarmed
* It must not assume the system is armed
* Sensor events are ignored or logged
* A correct PIN is required to return to `Disarmed`

---

## Required Demonstration

The project must show:

* Cluster startup with multiple nodes
* Keypad commands sent from a node
* Sensor events sent from a different node
* Message routing through cluster sharding
* Correct alarm state transitions
* Correct recovery behavior after entity restart or recreation
