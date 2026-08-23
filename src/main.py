"""Small FastAPI event-ingestion boundary used by SKYCOIN4444 observability services."""
from __future__ import annotations

from collections import Counter, deque
from time import time
from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(title="Metrics Aggregator", version="3.1.0")

class EventRequest(BaseModel):
    event_type: str = Field(min_length=1, max_length=100)
    payload: dict = Field(default_factory=dict)
    source: str = Field(min_length=1, max_length=100)

events_store: deque[dict] = deque(maxlen=1000)

@app.post("/api/v1/events", status_code=201)
def publish_event(event: EventRequest):
    record = {"event_type": event.event_type.strip(), "payload": event.payload, "source": event.source.strip(), "timestamp": int(time())}
    events_store.append(record)
    return {"status": "published", "event_type": record["event_type"], "total_events": len(events_store)}

@app.get("/api/v1/events")
def list_events():
    return {"events": list(events_store)[-10:], "total": len(events_store)}

@app.get("/api/v1/metrics")
def metrics():
    counts = Counter(event["event_type"] for event in events_store)
    return {"total_events": len(events_store), "by_event_type": dict(counts)}

@app.get("/health")
def health():
    return {"status": "healthy", "service": "metrics-aggregator", "timestamp": int(time())}
