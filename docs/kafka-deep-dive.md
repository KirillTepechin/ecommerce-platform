# Kafka Deep Dive Runbook

## 1) Multi-broker cluster and replication

Start cluster:

```bash
docker compose -f docker-compose-kafka/docker-compose.yml up -d
```

Verify RF=3 and partitions=12:

```bash
docker exec kafka-1 kafka-topics --bootstrap-server kafka-1:9092 --describe --topic order-created
docker exec kafka-1 kafka-topics --bootstrap-server kafka-1:9092 --describe --topic order-events-dlq
```

## 2) Partitions and custom partitioner

`order-service` uses `CustomerPartitioner`, and producer key is `customerId` (stable order affinity by customer).

Run throughput test:

```bash
./scripts/kafka-producer-throughput.sh
```

## 3) Consumer groups and rebalancing

Run 3 instances of order-service:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew :order-service:bootRun
SPRING_PROFILES_ACTIVE=local SERVER_PORT=0 ./gradlew :order-service:bootRun
SPRING_PROFILES_ACTIVE=local SERVER_PORT=0 ./gradlew :order-service:bootRun
```

Observe group state and rebalance:

```bash
docker exec kafka-1 kafka-consumer-groups --bootstrap-server kafka-1:9092 --describe --group order-service-group --members --state
```

Measure lag:

```bash
docker exec kafka-1 kafka-consumer-groups --bootstrap-server kafka-1:9092 --describe --group order-service-group
```

## 4) Dead Letter Queue and retry

Inventory consumer has retry (3 attempts x 2s) and forwards poison messages to `order-events-dlq`.

Inspect DLQ:

```bash
docker exec kafka-1 kafka-console-consumer --bootstrap-server kafka-1:9092 --topic order-events-dlq --from-beginning
```

## 5) Kafka Streams analytics-service

`analytics-service` builds hourly aggregations from `inventory-reserved` and stores metrics to MongoDB (`ep-analytics.hourly_order_metrics`):
- total sales
- avg order value
- number of orders

Run:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew :analytics-service:bootRun
```

## 6) Exactly-once semantics

- `order-service` producer is idempotent with `acks=all`.
- Transactional outbox (`order_outbox`) stores order events inside DB transaction.
- Scheduled outbox publisher sends events to Kafka in producer transaction.

Check stored metrics:

```bash
docker exec mongodb mongosh ep-analytics --eval "db.hourly_order_metrics.find().pretty()"
```
