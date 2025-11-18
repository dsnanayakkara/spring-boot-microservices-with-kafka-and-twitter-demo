# System Architecture

## Overview

This is a high-throughput microservices architecture demonstrating event-driven patterns using Apache Kafka, real-time stream processing, full-text search with Elasticsearch, and RESTful APIs.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          EVENT GENERATION LAYER                               │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    Event Stream Service (Port 8080)
                    - Generates 60 events/min
                    - Avro serialization
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           KAFKA CLUSTER (3 Brokers)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                      │
│  │   Broker 1   │  │   Broker 2   │  │   Broker 3   │                      │
│  │  Port 19092  │  │  Port 29092  │  │  Port 39092  │                      │
│  └──────────────┘  └──────────────┘  └──────────────┘                      │
│                                                                               │
│  Topics:                                                                      │
│  - social-events (3 partitions, RF=3)                                        │
│  - social-events-filtered                                                    │
│  - social-events-word-count                                                  │
│                                                                               │
│  Schema Registry (Port 8081) - Avro Schema Management                        │
└─────────────────────────────────────────────────────────────────────────────┘
                     │                    │                    │
         ┌───────────┴────────────────────┴────────────┐      │
         │                                               │      │
         ▼                                               ▼      ▼
┌────────────────────┐                    ┌───────────────────────────┐
│  Consumer Service  │                    │  Kafka Streams Service   │
│    (Port 8081)     │                    │      (Port 8082)          │
│                    │                    │                           │
│ - Batch processing │                    │ - Stream processing       │
│ - 3 threads        │                    │ - Word count (5-min)      │
│ - Metrics tracking │                    │ - Aggregations            │
│ - 500 records/poll │                    │ - 2 stream threads        │
└────────────────────┘                    └───────────────────────────┘
                                                     │
                                                     ▼
                                          Output Topics:
                                          - social-events-filtered
                                          - social-events-word-count

         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│               ELASTICSEARCH INDEXING SERVICE (Port 8083)                      │
│  - Consumes from social-events topic                                         │
│  - Transforms Avro → Elasticsearch documents                                 │
│  - Batch indexing                                                            │
│  - Consumer group: elasticsearch-consumer-group                              │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        ELASTICSEARCH CLUSTER                                  │
│  ┌────────────────────────────────────────────────────────────┐             │
│  │ Elasticsearch (Port 9200)                                   │             │
│  │ - Index: social-events-index                                │             │
│  │ - 3 shards, 1 replica                                       │             │
│  │ - Full-text search on event text                            │             │
│  │ - Time-based queries                                        │             │
│  └────────────────────────────────────────────────────────────┘             │
│                                                                               │
│  ┌────────────────────────────────────────────────────────────┐             │
│  │ Kibana (Port 5601)                                          │             │
│  │ - Visualization & dashboards                                │             │
│  │ - Index management                                          │             │
│  └────────────────────────────────────────────────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       REST API SERVICE (Port 8084)                            │
│  ┌────────────────────────────────────────────────────────────┐             │
│  │ API Endpoints:                                              │             │
│  │ - GET /api/v1/events/{id}                                   │             │
│  │ - GET /api/v1/events                                        │             │
│  │ - GET /api/v1/events/search?text={text}                     │             │
│  │ - GET /api/v1/events/user/{userId}                          │             │
│  │                                                              │             │
│  │ Features:                                                    │             │
│  │ - Pagination & sorting                                       │             │
│  │ - Full-text search                                           │             │
│  │ - OpenAPI/Swagger UI                                         │             │
│  └────────────────────────────────────────────────────────────┘             │
│                                                                               │
│  Swagger UI: http://localhost:8084/swagger-ui.html                           │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ▼
   ┌────────────┐
   │  Clients   │
   │  (Future)  │
   └────────────┘
```

## Data Flow

### 1. Event Generation
- **Event Stream Service** generates mock social events
- Events include: userId, id, text, createdAt
- Serialized using **Avro** schema
- Published to `social-events` topic

### 2. Parallel Processing
Events are consumed by **three independent services**:

#### A. Consumer Service
- General-purpose event processing
- Logs events with statistics
- Tracks metrics (consumed, processed, failed)
- Batch processing (500 records/poll)

#### B. Streams Service
- Real-time stream processing
- Filters events with text content
- Extracts words and performs:
  - Word count with 5-minute tumbling windows
  - User event count aggregation
- Outputs to derived topics

#### C. Elasticsearch Indexing Service
- Transforms Avro models to Elasticsearch documents
- Batch indexes to `social-events-index`
- Enables full-text search capabilities

### 3. Query Layer
- **REST API Service** provides HTTP endpoints
- Queries Elasticsearch for:
  - Individual event retrieval
  - Paginated event lists
  - Full-text search
  - User-based filtering

## Technology Stack

### Core Technologies
- **Java 21 (LTS)** - Latest LTS version with virtual threads support
- **Spring Boot 3.2.5** - Latest stable Spring Boot
- **Spring Framework 6.1.x** - Modern Spring with Jakarta EE 10
- **Maven** - Build and dependency management

### Messaging & Streaming
- **Apache Kafka** - Distributed event streaming (3 brokers)
- **Apache Avro 1.11.3** - Efficient data serialization
- **Confluent Platform 7.6.0** - Schema Registry
- **Spring Kafka 3.1.4** - Kafka integration
- **Kafka Streams** - Real-time stream processing

### Search & Storage
- **Elasticsearch 8.11.0** - Full-text search engine
- **Kibana 8.11.0** - Visualization platform
- **Spring Data Elasticsearch** - Elasticsearch integration

### API & Documentation
- **Spring Web (MVC)** - REST API framework
- **SpringDoc OpenAPI 2.3.0** - API documentation
- **Swagger UI** - Interactive API testing

### Monitoring & Operations
- **Spring Boot Actuator** - Production metrics
- **Micrometer** - Metrics facade
- **Prometheus** - Metrics export

### Infrastructure
- **Docker & Docker Compose** - Containerization
- **Zookeeper** - Kafka coordination

## Service Ports

| Service | Port | Purpose |
|---------|------|---------|
| Event Stream Service | 8080 | Producer - Event generation |
| Kafka Consumer Service | 8081 | Consumer - Event processing |
| Kafka Streams Service | 8082 | Stream processing |
| Elasticsearch Service | 8083 | Indexing service |
| REST API Service | 8084 | Query API |
| Kafka Broker 1 | 19092 | Message broker |
| Kafka Broker 2 | 29092 | Message broker |
| Kafka Broker 3 | 39092 | Message broker |
| Zookeeper | 2181 | Coordination |
| Schema Registry | 8081 | Avro schemas |
| Elasticsearch | 9200 | Search engine |
| Kibana | 5601 | Visualization |

## Scalability Features

### Kafka
- **3 brokers** for high availability
- **3 partitions per topic** for parallelism
- **Replication factor 3** for fault tolerance
- **Multiple consumer groups** for independent processing

### Elasticsearch
- **3 shards** for distributed search
- **1 replica** for fault tolerance
- **Configurable refresh interval** (1s)

### Services
- **Concurrent consumers** (3 threads per service)
- **Batch processing** (up to 500 records)
- **Stateless design** for horizontal scaling
- **Health checks** for orchestration

## Key Design Patterns

### 1. Event-Driven Architecture
- Asynchronous message-driven communication
- Loose coupling between services
- Event sourcing for audit trail

### 2. CQRS (Command Query Responsibility Segregation)
- Write path: Kafka → Elasticsearch (indexing)
- Read path: REST API → Elasticsearch (queries)
- Separate models for reads and writes

### 3. Stream Processing
- Real-time analytics with Kafka Streams
- Stateful aggregations
- Time-windowed operations

### 4. API Gateway Pattern
- Single REST API for all queries
- Pagination and filtering
- Standardized response format

## Monitoring & Observability

### Health Checks
All services expose `/actuator/health` endpoints:
- Kafka connectivity
- Elasticsearch connectivity
- JVM metrics
- Custom health indicators

### Metrics
Prometheus-compatible metrics at `/actuator/prometheus`:
- Message throughput
- Processing latency
- Consumer lag
- Error rates
- JVM memory and GC

### Logging
Structured logging with:
- Request/response logging
- Performance statistics
- Error tracking
- Batch processing metrics

## Future Enhancements

### Phase 5: Dashboard UI (Marked for Future)
- React 18 + Vite application
- Real-time event visualization
- Charts and analytics (Recharts)
- Auto-refresh from REST API
- User-friendly interface

### Additional Improvements
- Authentication & Authorization (OAuth 2.0)
- Rate limiting
- Caching layer (Redis)
- Message encryption
- Dead letter queues
- Circuit breakers
- Distributed tracing (OpenTelemetry)

## Performance Characteristics

### Throughput
- **Event Generation**: 60 events/minute (configurable)
- **Kafka**: Supports thousands of messages/second
- **Consumer Processing**: 500 records per batch
- **Elasticsearch Indexing**: Batch indexing for efficiency

### Latency
- **End-to-end**: <5 seconds (producer → indexed)
- **Query Response**: <100ms (simple queries)
- **Stream Processing**: Near real-time (<1s)

### Availability
- **Kafka**: Survives 1 broker failure (RF=3)
- **Elasticsearch**: Survives 1 shard failure
- **Services**: Stateless - can restart anytime
