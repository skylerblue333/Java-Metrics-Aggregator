package com.skycoin4444.metrics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws IOException {
        int port = parseInt("PORT", 8080, 1, 65535);
        int maxMetrics = parseInt("MAX_METRICS", 1000, 1, 10000);
        MetricsAggregator aggregator = new MetricsAggregator(maxMetrics);
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/healthz", exchange -> json(exchange, 200, "{\"status\":\"ok\"}"));
        server.createContext("/readyz", exchange -> json(exchange, 200, "{\"ready\":true}"));
        server.createContext("/metrics", exchange -> handleMetrics(exchange, aggregator));
        server.start();
        System.out.println("{\"event\":\"server_started\",\"service\":\"sky-metrics-aggregator\",\"port\":" + port + "}");
    }

    private static void handleMetrics(HttpExchange exchange, MetricsAggregator aggregator) throws IOException {
        try {
            Map<String, String> q = query(exchange.getRequestURI().getRawQuery());
            String name = q.get("name");
            if ("POST".equals(exchange.getRequestMethod())) {
                double value = Double.parseDouble(q.getOrDefault("value", "NaN"));
                aggregator.record(name, value);
                json(exchange, 202, "{\"accepted\":true}");
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                MetricsAggregator.Summary s = aggregator.summary(name);
                String body = "{\"count\":" + s.count() + ",\"sum\":" + finiteJson(s.sum()) + ",\"min\":" + finiteJson(s.min()) + ",\"max\":" + finiteJson(s.max()) + ",\"average\":" + finiteJson(s.average()) + "}";
                json(exchange, 200, body);
                return;
            }
            json(exchange, 405, "{\"error\":\"method_not_allowed\"}");
        } catch (RuntimeException e) {
            json(exchange, 400, "{\"error\":\"invalid_request\"}");
        }
    }

    private static String finiteJson(double value) { return Double.isFinite(value) ? Double.toString(value) : "null"; }

    private static Map<String, String> query(String raw) {
        Map<String, String> values = new HashMap<>();
        if (raw == null || raw.isBlank()) return values;
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            values.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
        }
        return values;
    }

    private static int parseInt(String name, int fallback, int min, int max) {
        String raw = System.getenv(name);
        int value = raw == null ? fallback : Integer.parseInt(raw);
        if (value < min || value > max) throw new IllegalArgumentException(name + " out of range");
        return value;
    }

    private static void json(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
