"""
Java-Metrics-Aggregator: FastAPI microservice implementing Java Metrics Aggregator domain logic
"""
import time
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Java-Metrics-Aggregator", version="3.0.0")

class EventRequest(BaseModel):
    event_type: str
    payload: dict
    source: str

events_store = []

@app.post("/api/v1/events")
def publish_event(event: EventRequest):
    events_store.append(event.dict())
    return {"status": "published", "event_type": event.event_type, "total_events": len(events_store)}

@app.get("/api/v1/events")
def list_events():
    return {"events": events_store[-10:], "total": len(events_store)}


@app.get("/health")
def health():
    return {"status": "healthy", "service": "Java-Metrics-Aggregator", "timestamp": int(time.time())}
