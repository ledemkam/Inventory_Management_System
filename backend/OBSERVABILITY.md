# Observability (OpenTelemetry)

The backend exports **distributed traces** and **metrics** over OTLP (OpenTelemetry protocol) using Micrometer
Observation + Micrometer Tracing. Actuator provides the underlying instrumentation; nothing replaces it.

## What is instrumented out of the box

- HTTP server requests (one span per request, with status / route / exception)
- `RestClient` / `RestTemplate` / `WebClient` outgoing calls
- Spring Data JPA repository calls and JDBC statements
- Spring Security filter chain, `@Scheduled`, `@Async`, cache access
- JVM, HikariCP pool, HTTP, Tomcat and Logback metrics
- Log lines carry `[traceId,spanId]` so logs correlate with traces

Custom business spans/metrics are **not** added yet. When the transaction/stock logic stabilises, annotate service
methods with `@Observed(name = "...")` (the aspect is already enabled via
`management.observations.annotations.enabled=true`) or inject
`ObservationRegistry` / `MeterRegistry`.

## Run it locally

```bash
# from backend/
DB_USERNAME=postgres DB_PASSWORD=postgres docker compose up -d
./mvnw spring-boot:run           # development profile is active by default
```

- Grafana UI: http://localhost:3000 (anonymous admin, no login)
    - Explore → **Tempo** for traces, **Prometheus** for metrics, **Loki** for logs
- OTLP receiver: `http://localhost:4318` (HTTP) / `4317` (gRPC)

The `development` profile traces every request (`sampling.probability = 1.0`) and pushes metrics every 30s.

If you want to run the app **without** the observability stack, disable export:

```bash
MANAGEMENT_TRACING_ENABLED=false MANAGEMENT_OTLP_METRICS_ENABLED=false ./mvnw spring-boot:run
```

## Configuration

All keys live in `application*.yaml` under `management.*`. Override per environment:

| Env var                                   | Default                             | Meaning                                                                |
|-------------------------------------------|-------------------------------------|------------------------------------------------------------------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT`             | `http://localhost:4318`             | Base URL of the collector; `/v1/traces` and `/v1/metrics` are appended |
| `MANAGEMENT_TRACING_ENABLED`              | `true`                              | Master switch for tracing                                              |
| `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` | `0.1` (base) / `1.0` (dev)          | Fraction of traces sampled                                             |
| `MANAGEMENT_OTLP_METRICS_ENABLED`         | `false` (base) / `true` (dev, prod) | Push metrics over OTLP                                                 |

## Production

Set `SPRING_PROFILES_ACTIVE=production` and `OTEL_EXPORTER_OTLP_ENDPOINT` to the real collector. Sampling defaults to
10%; raise `MANAGEMENT_TRACING_SAMPLING_PROBABILITY`
only if the collector and cost budget allow it.

## Tests

Telemetry is fully disabled in the test profile (`src/test/resources/application.yaml`:
`management.tracing.enabled=false`, metrics export off) so the build needs no collector.
