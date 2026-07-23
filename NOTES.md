



A ride-sharing backend built with Spring Boot microservices (Java 17, Spring Boot 4), coordinated via Kafka with Redis + MySQL.


[Client]
   |
   ├── location-service   (Redis + REST)
   ├── matching-service   (Kafka)
   └── ride-service       (MySQL + Kafka + JPA)


## Services

### location-service
- **Port:** TBD
- **Storage:** Redis (geo-spatial — `GEOADD` / `GEORADIUS` style commands)
- **Role:** Receives real-time driver GPS pings, stores them in Redis, and exposes a nearby-driver search endpoint.
- **Key DTOs:**
  - `DriverLocationRequest` — `driverId`, `latitude`, `longitude` (Lombok: `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`)
  - `NearByDriverResponse` — `driverId`, `latitude`, `longitude`, `distanceInKm` (Lombok: `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`)
- **Controller:** `LocationController` — base path `/api/v1/locations`
  - `POST /drivers/update` — accepts `DriverLocationRequest`; returns hardcoded `"driver location updated"` string — does **not** call the service layer
  - `GET /drivers/nearby?latitude=&longitude=&radius=` — delegates to `LocationService.findNearbyDrivers`; `radius` defaults to `5.0` km
  - `DELETE /drivers/{driverID}` — delegates to `LocationService.removeDriver`
- **Service:** `LocationService` — two methods only (`findNearbyDrivers` returns `List.of()`, `removeDriver` is no-op); no `updateDriverLocation` method exists yet; Redis GEO ops not yet wired.
- **Config:** `application.yaml` has `spring.application.name` only — Redis host/port not configured.
- **Status:** Controller + DTOs done. Service layer is a stub — Redis integration pending; `updateDriverLocation` is not yet wired to service.

### matching-service
- **Storage:** None (stateless)
- **Messaging:** Kafka consumer + producer (`spring-kafka` dependency declared, not used)
- **Role:** Listens for ride-request events, queries location-service for nearby drivers, and publishes a match event.
- **Config:** `application.yaml` has `spring.application.name` only — no Kafka broker configured.
- **Status:** Skeleton only — main class exists; no controllers, services, DTOs, either Kafka wiring.

### ride-service
- **Storage:** MySQL (`ride_db`)
- **Messaging:** Kafka producer (publishes ride-request events), consumer (listens for match events)
- **Role:** Entry point for rider requests; persists ride state; triggers the matching flow.
- **Dependencies declared:** `spring-boot-starter-data-jpa`, `spring-kafka`, `mysql-connector-j`, `spring-boot-starter-validation` — none wired yet.
- **Config:** `application.yaml` has `spring.application.name` only — no datasource or Kafka configured.
- **Status:** Skeleton only — main class exists; no entities, repositories, controllers, or Kafka wiring.

## Infrastructure (docker-compose.yml)

| Service    | Image                          | Port  | Purpose                         |
|------------|-------------------------------|-------|---------------------------------|
| Redis      | redis:latest                  | 6379  | Driver location store (geo)     |
| MySQL      | mysql:8.0                     | 3306  | Ride persistence (`ride_db`)    |
| Zookeeper  | confluentinc/cp-zookeeper:7.4 | 2181  | Kafka coordinator               |
| Kafka      | confluentinc/cp-kafka:7.4     | 9092  | Event streaming between services|



## Event Flow

1. **Rider** calls `ride-service` → creates a ride record in MySQL → publishes `ride.requested` Kafka topic.
2. **matching-service** consumes `ride.requested` → calls `location-service` to find nearby drivers → publishes `ride.matched`.
3. **ride-service** consumes `ride.matched` → updates ride record with assigned driver.
4. **Driver app** periodically POSTs GPS coords to `location-service` → stored in Redis GEO set.



### location-service
- [ ] Add `updateDriverLocation` method to `LocationService` and call it from `LocationController.updateDriverLocation()` (currently returns hardcoded string, service not invoked)
- [ ] `LocationService.updateDriverLocation`: wire Redis `GEOADD`
- [ ] `LocationService.findNearbyDrivers`: implement Redis `GEORADIUS` / `GEOSEARCH` and map results to `NearByDriverResponse`
- [ ] `LocationService.removeDriver`: implement Redis `ZREM` / `HDEL`
- [ ] Configure `application.yaml`: `spring.data.redis.host` + `port`

### ride-service
- [ ] Define `Ride` JPA entity + repository
- [ ] Add Kafka producer for `ride.requested` topic
- [ ] Add Kafka consumer for `ride.matched` topic (update ride record with assigned driver)
- [ ] Configure `application.yaml`: MySQL datasource URL, JPA settings, Kafka brokers

### matching-service
- [ ] Add Kafka consumer for `ride.requested`
- [ ] Call `location-service` to find nearby drivers (WebClient / Feign)
- [ ] Publish `ride.matched` Kafka event
- [ ] Configure `application.yaml`: Kafka brokers, location-service URL

### Cross-cutting
- [ ] Wire inter-service HTTP calls (WebClient / Feign) for matching-service → location-service
- [ ] Assign ports to location-service and matching-service in their `application.yaml`

## Running Locally

MySQL credentials: `root / root`, database: `ride_db`
Kafka broker: `localhost:9092`
Redis: `localhost:6379`
