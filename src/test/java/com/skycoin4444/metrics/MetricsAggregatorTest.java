package com.skycoin4444.metrics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MetricsAggregatorTest {
    @Test
    void aggregatesDeterministicSummary() {
        MetricsAggregator aggregator = new MetricsAggregator(10);
        aggregator.record("api.latency_ms", 10.0);
        aggregator.record("api.latency_ms", 20.0);
        MetricsAggregator.Summary s = aggregator.summary("api.latency_ms");
        assertEquals(2, s.count());
        assertEquals(30.0, s.sum());
        assertEquals(10.0, s.min());
        assertEquals(20.0, s.max());
        assertEquals(15.0, s.average());
    }

    @Test
    void enforcesCardinalityAndFiniteValues() {
        MetricsAggregator aggregator = new MetricsAggregator(1);
        aggregator.record("cpu", 1.0);
        assertThrows(IllegalStateException.class, () -> aggregator.record("memory", 2.0));
        assertThrows(IllegalArgumentException.class, () -> aggregator.record("cpu", Double.NaN));
    }

    @Test
    void emptySummaryIsExplicit() {
        MetricsAggregator.Summary s = new MetricsAggregator(2).summary("missing");
        assertEquals(0, s.count());
        assertTrue(Double.isNaN(s.average()));
    }
}
