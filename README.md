# Sky Metrics Aggregator (Java)

**Status: engineering beta.** A focused Java 21 in-memory numeric metrics aggregation service for the SKYCOIN4444 observability boundary.

## Implemented

- concurrent per-metric count, sum, min, max, and average
- bounded metric names (`[A-Za-z][A-Za-z0-9_.-]{0,63}`)
- finite-value validation
- configurable metric-cardinality limit (`MAX_METRICS`, default 1000)
- `POST /metrics?name=<metric>&value=<number>` ingestion
- `GET /metrics?name=<metric>` summary retrieval
- `GET /healthz` and `GET /readyz`
- JUnit tests, Maven verification, dependency scanning
- non-root container packaging and CI smoke health check

## Run locally

```bash
mvn clean verify
mvn -DskipTests package
java -jar target/sky-metrics-aggregator-0.1.0.jar
```

Then:

```bash
curl -X POST 'http://localhost:8080/metrics?name=api.latency_ms&value=42.5'
curl 'http://localhost:8080/metrics?name=api.latency_ms'
```

## Boundaries

This is not a Prometheus replacement, OpenTelemetry collector, durable time-series database, distributed metrics cluster, or verified production deployment. State is process-local and in memory. It has no authentication, persistence, replication, multi-tenant isolation, TLS termination, retention tiers, histograms/percentiles, or cross-node aggregation.

For production integration, use this repository as a small aggregation/service pattern or adapter around established observability infrastructure rather than as a substitute for a durable metrics backend.

## SKYCOIN4444 role

`SKYCOIN4444 → Infrastructure/Observability → Metrics aggregation boundary`

## License

See `LICENSE`.
