package com.skycoin4444.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;

public final class MetricsAggregator {
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z0-9_.-]{0,63}");
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int maxMetrics;

    public MetricsAggregator(int maxMetrics) {
        if (maxMetrics < 1 || maxMetrics > 10_000) throw new IllegalArgumentException("maxMetrics must be 1-10000");
        this.maxMetrics = maxMetrics;
    }

    public void record(String name, double value) {
        validateName(name);
        if (!Double.isFinite(value)) throw new IllegalArgumentException("value must be finite");
        Bucket existing = buckets.get(name);
        if (existing == null && buckets.size() >= maxMetrics) throw new IllegalStateException("metric cardinality limit reached");
        buckets.computeIfAbsent(name, ignored -> new Bucket()).add(value);
    }

    public Summary summary(String name) {
        validateName(name);
        Bucket bucket = buckets.get(name);
        if (bucket == null) return new Summary(0, 0, Double.NaN, Double.NaN, Double.NaN);
        return bucket.snapshot();
    }

    public int metricCount() { return buckets.size(); }

    private static void validateName(String name) {
        if (name == null || !NAME.matcher(name).matches()) throw new IllegalArgumentException("invalid metric name");
    }

    public record Summary(long count, double sum, double min, double max, double average) {}

    private static final class Bucket {
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final DoubleAccumulator min = new DoubleAccumulator(Math::min, Double.POSITIVE_INFINITY);
        private final DoubleAccumulator max = new DoubleAccumulator(Math::max, Double.NEGATIVE_INFINITY);

        void add(double value) {
            count.increment(); sum.add(value); min.accumulate(value); max.accumulate(value);
        }

        Summary snapshot() {
            long c = count.sum();
            double s = sum.sum();
            return new Summary(c, s, min.get(), max.get(), c == 0 ? Double.NaN : s / c);
        }
    }
}
