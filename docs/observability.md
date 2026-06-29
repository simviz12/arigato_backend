# OBSERVABILITY & METRICS CATALOG (Week 9)

## Overview
This document outlines the telemetry and observability tools implemented to monitor the health, performance, and business KPIs of the Arigato Restaurant Management System in production.

## 1. Structured Logging (Logstash)
All logs are emitted in JSON format via `logstash-logback-encoder`.
- **Traceability**: A `traceId` (Correlation ID) is injected into every log entry via SLF4J MDC. A single request from a cashier generates a unique UUID (e.g., `X-Request-ID: 1234-5678`) that ties together every log line, DB query, and error thrown during that exact lifecycle, cutting debugging time from hours to seconds.

## 2. Health & Liveness Probes
Exposed securely via Spring Boot Actuator:
- **`GET /actuator/health`**: Used by the load balancer/Kubernetes to check if the app is alive. It automatically pings PostgreSQL to ensure DB connectivity is active, and verifies the server hasn't run out of disk space.

## 3. Micrometer & Prometheus Metrics
Exposed via `GET /actuator/prometheus` to be scraped by a Prometheus instance and visualized in Grafana.

### Custom Business Metrics Catalog
| Metric Name | Type | Description |
|---|---|---|
| `sales.processed` | Counter | Total number of successful sales completed. Used with rate() to calculate *Sales per Minute*. |
| `sales.value` | Summary | Distribution of the total monetary value of sales (Averages, Max, Min). |
| `inventory.lock.retries` | Counter | Increments whenever an Optimistic Locking collision occurs (Week 5). **Alerting Rule**: If this spikes above 10/min, it indicates severe concurrency contention on popular ingredients (e.g., Tomate). |
| `pdf.generation.duration` | Timer | Tracks how long the OpenPDF library takes to generate the "Mejores Proveedores" report. **Alerting Rule**: If the 99th percentile goes above 5 seconds, it indicates the catalog has grown too large for synchronous generation. |

## 4. Security Note
By default, `/actuator/health` and `/actuator/prometheus` are whitelisted in `SecurityConfig.java` to allow monitoring tools (which usually don't carry JWT tokens) to scrape them from inside the private VPC network. All other actuator endpoints remain protected.
