# Uber App — Project Notes

## Architecture Overview

A ride-sharing backend built as three Spring Boot microservices (Java 17, Spring Boot 4.x), coordinated via Kafka and backed by Redis + MySQL.

```
[Client]
   |
   ├── location-service   (Redis + REST)
   ├── matching-service   (Kafka consumer/producer)
   └── ride-service       (MySQL + Kafka + JPA)
```

## Services

### location-service
- **Port:** TBD
- **Storage:** Redis (geo-spatial — `GEOADD` / `GEORADIUS` style commands)
- **Role:** Receives real-time driver GPS pings, stores them in Redis, and exposes a nearby-driver search endpoint.
- **Key DTOs:**
  - `DriverLocationRequest` — `driverId`, `latitude`, `longitude`
  - `NearByDriverResponse` — `driverId`, `latitude`, `longitude`, `distanceInKm`
- **Status:** DTOs defined; controller/service layer not yet implemented.

### matching-service
- **Storage:** None (stateless)
- **Messaging:** Kafka consumer + producer
- **Role:** Listens for ride-request events, queries location-service for nearby drivers, and publishes a match event.
- **Status:** Scaffold only — business logic not yet added.

### ride-service
- **Storage:** MySQL (`ride_db`)
- **Messaging:** Kafka producer (publishes ride-request events), consumer (listens for match events)
- **Role:** Entry point for rider requests; persists ride state; triggers the matching flow.
- **Status:** Scaffold only — JPA entities and Kafka wiring not yet added.

## Infrastructure (docker-compose.yml)

| Service    | Image                          | Port  | Purpose                         |
|------------|-------------------------------|-------|---------------------------------|
| Redis      | redis:latest                  | 6379  | Driver location store (geo)     |
| MySQL      | mysql:8.0                     | 3306  | Ride persistence (`ride_db`)    |
| Zookeeper  | confluentinc/cp-zookeeper:7.4 | 2181  | Kafka coordinator               |
| Kafka      | confluentinc/cp-kafka:7.4     | 9092  | Event streaming between services|

All containers share the `rideshare-network` bridge network.

## Typical Event Flow

1. **Rider** calls `ride-service` → creates a ride record in MySQL → publishes `ride.requested` Kafka topic.
2. **matching-service** consumes `ride.requested` → calls `location-service` to find nearby drivers → publishes `ride.matched`.
3. **ride-service** consumes `ride.matched` → updates ride record with assigned driver.
4. **Driver app** periodically POSTs GPS coords to `location-service` → stored in Redis GEO set.

## What's Next (TODO)

- [ ] `location-service`: Add `DriverLocationController` (POST `/drivers/location`) and `LocationService` (Redis GEO ops)
- [ ] `location-service`: Add `NearByDriverController` (GET `/drivers/nearby`) using Redis `GEORADIUS`
- [ ] Fix `NearByDriverResponse` — add Lombok `@Data`/`@AllArgsConstructor`/`@NoArgsConstructor` (currently missing)
- [ ] `ride-service`: Define `Ride` JPA entity + repository
- [ ] `ride-service`: Add Kafka producer for `ride.requested` topic
- [ ] `matching-service`: Add Kafka consumer for `ride.requested`, call location-service, publish `ride.matched`
- [ ] Add `application.yml` / `application.properties` for each service (Redis host, Kafka brokers, MySQL URL)
- [ ] Wire services together with inter-service HTTP (WebClient / Feign) or Kafka messaging

## Running Locally

```bash
# Start infrastructure
docker-compose up -d

# Run each service (from its own directory)
./mvnw spring-boot:run
```

MySQL credentials: `root / root`, database: `ride_db`
Kafka broker (from host): `localhost:9092`
Redis: `localhost:6379`
