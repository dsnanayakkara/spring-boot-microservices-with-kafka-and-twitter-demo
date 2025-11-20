# Quick Reference Guide

A comprehensive cheat sheet for Spring Boot Microservices with Kafka.

---

## Kafka CLI Commands

### Topic Management

```bash
# List all topics
docker exec kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# Create topic
docker exec kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic my-topic \
  --partitions 3 \
  --replication-factor 3

# Describe topic
docker exec kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic social-events

# Delete topic
docker exec kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic my-topic
```

### Producer / Consumer

```bash
# Produce messages (console)
docker exec -it kafka-broker-1 kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic social-events \
  --property "key.separator=:" \
  --property "parse.key=true"

# Consume from beginning
docker exec kafka-broker-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic social-events \
  --from-beginning \
  --max-messages 10

# Consume with key
docker exec kafka-broker-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic social-events \
  --property print.key=true \
  --property key.separator=":"

# Consume specific partition
docker exec kafka-broker-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic social-events \
  --partition 0 \
  --offset earliest
```

### Consumer Groups

```bash
# List consumer groups
docker exec kafka-broker-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Describe consumer group
docker exec kafka-broker-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group social-events-consumer-group

# Reset offsets (to beginning)
docker exec kafka-broker-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group my-group \
  --reset-offsets \
  --to-earliest \
  --topic social-events \
  --execute

# Reset offsets (to specific offset)
docker exec kafka-broker-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group my-group \
  --reset-offsets \
  --to-offset 100 \
  --topic social-events:0 \
  --execute
```

---

## Elasticsearch Commands

### Index Management

```bash
# List indices
curl http://localhost:9200/_cat/indices?v

# Get index info
curl http://localhost:9200/social-events-index

# Delete index
curl -X DELETE http://localhost:9200/social-events-index

# Create index with settings
curl -X PUT http://localhost:9200/my-index \
  -H 'Content-Type: application/json' \
  -d '{
    "settings": {
      "number_of_shards": 3,
      "number_of_replicas": 1
    }
  }'
```

### Document Operations

```bash
# Index document
curl -X POST http://localhost:9200/social-events-index/_doc \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 42,
    "text": "Hello Elasticsearch",
    "createdAt": "2024-11-20T10:00:00Z"
  }'

# Get document by ID
curl http://localhost:9200/social-events-index/_doc/12345

# Update document
curl -X POST http://localhost:9200/social-events-index/_update/12345 \
  -H 'Content-Type: application/json' \
  -d '{
    "doc": {
      "text": "Updated text"
    }
  }'

# Delete document
curl -X DELETE http://localhost:9200/social-events-index/_doc/12345
```

### Search Queries

```bash
# Search all documents
curl http://localhost:9200/social-events-index/_search | jq

# Match query (full-text search)
curl http://localhost:9200/social-events-index/_search \
  -H 'Content-Type: application/json' \
  -d '{
    "query": {
      "match": {
        "text": "kafka"
      }
    }
  }' | jq

# Range query
curl http://localhost:9200/social-events-index/_search \
  -H 'Content-Type: application/json' \
  -d '{
    "query": {
      "range": {
        "createdAt": {
          "gte": "2024-11-20T00:00:00Z",
          "lte": "2024-11-20T23:59:59Z"
        }
      }
    }
  }' | jq

# Aggregation (count by user)
curl http://localhost:9200/social-events-index/_search \
  -H 'Content-Type: application/json' \
  -d '{
    "size": 0,
    "aggs": {
      "by_user": {
        "terms": {
          "field": "userId",
          "size": 10
        }
      }
    }
  }' | jq
```

---

## Schema Registry

```bash
# List all subjects
curl http://localhost:8081/subjects

# Get versions for subject
curl http://localhost:8081/subjects/social-events-value/versions

# Get specific version
curl http://localhost:8081/subjects/social-events-value/versions/1

# Get latest schema
curl http://localhost:8081/subjects/social-events-value/versions/latest

# Register new schema
curl -X POST http://localhost:8081/subjects/social-events-value/versions \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  -d '{
    "schema": "{\"type\":\"record\",\"name\":\"SocialEvent\",\"fields\":[{\"name\":\"id\",\"type\":\"long\"}]}"
  }'

# Check compatibility
curl -X POST http://localhost:8081/compatibility/subjects/social-events-value/versions/latest \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  -d '{
    "schema": "{\"type\":\"record\",...}"
  }'

# Delete subject
curl -X DELETE http://localhost:8081/subjects/social-events-value
```

---

## Spring Boot Actuator

```bash
# Health check
curl http://localhost:8080/actuator/health

# Detailed health
curl http://localhost:8080/actuator/health | jq

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/kafka.producer.sent

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Environment variables
curl http://localhost:8080/actuator/env

# Application info
curl http://localhost:8080/actuator/info

# Thread dump
curl http://localhost:8080/actuator/threaddump

# Heap dump
curl http://localhost:8080/actuator/heapdump -o heapdump.hprof
```

---

## Maven Commands

```bash
# Clean and build all modules
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run specific service
cd event-stream-service
mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests only
mvn test

# Generate Avro classes
cd kafka/kafka-model
mvn clean compile

# Package as JAR
mvn clean package

# Run JAR
java -jar target/event-stream-service-1.0.0.jar
```

---

## Docker Commands

### Infrastructure Management

```bash
# Start all services
cd docker-compose
docker-compose -f kafka_cluster.yml up -d

# Stop all services
docker-compose -f kafka_cluster.yml down

# View logs
docker-compose -f kafka_cluster.yml logs -f

# View logs for specific service
docker-compose -f kafka_cluster.yml logs -f kafka-broker-1

# Restart service
docker-compose -f kafka_cluster.yml restart kafka-broker-1

# Check status
docker-compose -f kafka_cluster.yml ps

# Remove volumes (clean slate)
docker-compose -f kafka_cluster.yml down -v
```

### Container Management

```bash
# List running containers
docker ps

# Execute command in container
docker exec -it kafka-broker-1 bash

# View container logs
docker logs kafka-broker-1 -f

# Inspect container
docker inspect kafka-broker-1

# Stop container
docker stop kafka-broker-1

# Start container
docker start kafka-broker-1

# Remove container
docker rm kafka-broker-1
```

---

## REST API Endpoints

### Elastic Query Service (Port 8084)

```bash
# Get all events (paginated)
curl http://localhost:8084/api/v1/events?page=0&size=20 | jq

# Get event by ID
curl http://localhost:8084/api/v1/events/1234567890 | jq

# Full-text search
curl 'http://localhost:8084/api/v1/events/search?text=kafka&page=0&size=10' | jq

# Get events by user
curl http://localhost:8084/api/v1/events/user/42?page=0&size=10 | jq

# Swagger UI
open http://localhost:8084/swagger-ui.html
```

---

## Common Configuration Properties

### Producer Configuration

```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:19092,localhost:29092,localhost:39092
      key-serializer: org.apache.kafka.common.serialization.LongSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      acks: all                    # Wait for all replicas
      retries: 3                   # Retry 3 times
      batch-size: 16384            # 16 KB batches
      linger-ms: 10                # Wait 10ms to batch
      compression-type: snappy     # Compression algorithm
      properties:
        schema.registry.url: http://localhost:8081
```

### Consumer Configuration

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:19092,localhost:29092,localhost:39092
      key-deserializer: org.apache.kafka.common.serialization.LongDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
      group-id: my-consumer-group
      auto-offset-reset: earliest  # Start from beginning
      enable-auto-commit: false    # Manual commit
      max-poll-records: 500        # Batch size
      properties:
        schema.registry.url: http://localhost:8081
        specific.avro.reader: true
```

### Kafka Streams Configuration

```yaml
spring:
  kafka:
    streams:
      application-id: my-streams-app
      bootstrap-servers: localhost:19092,localhost:29092,localhost:39092
      replication-factor: 3
      properties:
        default.key.serde: org.apache.kafka.common.serialization.Serdes$LongSerde
        default.value.serde: io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
        schema.registry.url: http://localhost:8081
        num.stream.threads: 3
        commit.interval.ms: 10000
```

---

## Troubleshooting

### Kafka Issues

**Problem**: Consumer lag growing

```bash
# Check consumer group lag
docker exec kafka-broker-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group social-events-consumer-group

# Look for LAG column
# If LAG > 1000, add more consumers or increase batch size
```

**Problem**: Topic not created

```bash
# Manually create topic
docker exec kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic social-events \
  --partitions 3 \
  --replication-factor 3

# Verify creation
docker exec kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic social-events
```

**Problem**: Producer can't connect

```bash
# Check if brokers are running
docker ps | grep kafka

# Check broker logs
docker logs kafka-broker-1

# Test connection
telnet localhost 19092
```

### Elasticsearch Issues

**Problem**: Index not created

```bash
# Check if Elasticsearch is running
curl http://localhost:9200

# Manually create index
curl -X PUT http://localhost:9200/social-events-index \
  -H 'Content-Type: application/json' \
  -d '{
    "settings": {
      "number_of_shards": 3,
      "number_of_replicas": 1
    }
  }'
```

**Problem**: Search not returning results

```bash
# Check document count
curl http://localhost:9200/social-events-index/_count

# If count is 0, check indexing service logs
cd elasticsearch-service
mvn spring-boot:run

# Look for indexing errors
```

### Service Issues

**Problem**: Service won't start

```bash
# Check if port is in use
lsof -i :8080

# Kill process using port
kill -9 <PID>

# Check application logs
cd event-stream-service
mvn spring-boot:run

# Look for stack trace
```

**Problem**: Out of memory

```bash
# Increase heap size
export MAVEN_OPTS="-Xmx2g"
mvn spring-boot:run

# Or in IntelliJ: Run > Edit Configurations > VM options: -Xmx2g
```

---

## Performance Tuning

### Producer Optimization

**High Throughput**:
```yaml
batch-size: 32768        # 32 KB
linger-ms: 20            # Wait longer
compression-type: lz4    # Fast compression
acks: 1                  # Leader only (lower durability)
```

**Low Latency**:
```yaml
batch-size: 0            # No batching
linger-ms: 0             # Send immediately
compression-type: none   # No compression
acks: 1                  # Leader only
```

**High Durability**:
```yaml
acks: all                # All replicas
min-insync-replicas: 2   # Minimum 2 replicas
retries: 10              # Retry more
```

### Consumer Optimization

**High Throughput**:
```yaml
max-poll-records: 1000   # Larger batches
fetch-min-bytes: 50000   # Wait for more data
concurrency: 10          # More threads
```

**Low Latency**:
```yaml
max-poll-records: 100    # Smaller batches
fetch-min-bytes: 1       # Don't wait
fetch-max-wait-ms: 100   # Return quickly
```

---

## Monitoring Queries

### Prometheus Metrics

```promql
# Producer send rate
rate(kafka_producer_sent_total[1m])

# Consumer lag
kafka_consumer_lag_total

# Stream processing rate
rate(kafka_streams_processed_total[1m])

# Error rate
rate(kafka_producer_failed_total[1m])
```

---

## Keyboard Shortcuts (IntelliJ IDEA)

- **Run Application**: `Ctrl+Shift+F10`
- **Stop Application**: `Ctrl+F2`
- **Debug**: `Shift+F9`
- **Find in Files**: `Ctrl+Shift+F`
- **Navigate to Class**: `Ctrl+N`
- **Navigate to File**: `Ctrl+Shift+N`
- **Reformat Code**: `Ctrl+Alt+L`

---

**Keep this guide handy for quick lookups!** 📚
