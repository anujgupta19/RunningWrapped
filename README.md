# RunningWrapped

Spotify-Wrapped-style recaps and progress analytics for runners: import your FIT / GPX / TCX files and see how far you've come.

- `backend/`: Java 21 + Spring Boot modular monolith
- `mobile/`: React Native + Expo + TypeScript
- `docs/`: [architecture](docs/ARCHITECTURE.md), [data model](docs/DATA_MODEL.md), [roadmap](docs/ROADMAP.md)

## Run the backend

```bash
cd backend
./mvnw spring-boot:run
curl localhost:8080/actuator/health
```
