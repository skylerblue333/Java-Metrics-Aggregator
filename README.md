# Metrics Aggregator

A small Python/FastAPI event-ingestion and aggregation component for the SKYCOIN4444 observability boundary.

## Implemented

- `POST /api/v1/events` for validated event ingestion
- bounded in-memory event retention (1,000 records)
- `GET /api/v1/events` for recent events
- `GET /api/v1/metrics` for counts by event type
- `GET /health` health endpoint
- Pydantic request validation

## Architecture role

This repository is a focused **metrics/event aggregation primitive**. It is not a production metrics database, Java application, hosted SaaS product, or enterprise observability platform by itself.

The implementation intentionally uses bounded in-memory storage. Production consolidation should replace that storage with a durable metrics/event backend and connect it to the canonical SKYCOIN4444 observability pipeline.

## Verification status

The repository contains CI/test infrastructure, but this README does not claim that every workflow currently passes. Production readiness, scalability, HA, external integrations, customers, ARR, and enterprise dependencies remain unverified.

## Consolidation target

`SKYCOIN4444 → Security/Infrastructure → Observability → Metrics/Event Aggregation`

Potential production foundations include OpenTelemetry and a durable metrics/event backend rather than a custom replacement for established infrastructure.
