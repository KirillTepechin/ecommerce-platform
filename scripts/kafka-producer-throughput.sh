#!/usr/bin/env bash
set -euo pipefail

# Requires running kafka cluster from docker-compose-kafka

docker exec kafka-1 kafka-producer-perf-test \
  --topic order-created \
  --num-records 200000 \
  --record-size 512 \
  --throughput -1 \
  --producer-props \
    acks=all \
    bootstrap.servers=kafka-1:9092,kafka-2:9092,kafka-3:9092 \
    linger.ms=5 \
    batch.size=65536 \
    compression.type=lz4
