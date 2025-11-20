# Module 2: Project Architecture & Setup

**Duration**: 45 minutes
**Difficulty**: Beginner
**Prerequisites**: Module 1 completed

---

## Learning Objectives

By the end of this module, you will understand:

✅ The overall system architecture and data flow
✅ Each microservice's role and responsibility
✅ How services communicate via Kafka
✅ The CQRS pattern implementation
✅ How to set up and run the project locally
✅ How to verify that all services are working

---

## Table of Contents

1. [System Architecture Overview](#1-system-architecture-overview)
2. [Microservices Breakdown](#2-microservices-breakdown)
3. [Data Flow Walkthrough](#3-data-flow-walkthrough)
4. [Infrastructure Components](#4-infrastructure-components)
5. [CQRS Pattern Implementation](#5-cqrs-pattern-implementation)
6. [Project Structure](#6-project-structure)
7. [Environment Setup](#7-environment-setup)
8. [Running the Project](#8-running-the-project)
9. [Verification and Testing](#9-verification-and-testing)
10. [Summary](#10-summary)

---

## 1. System Architecture Overview

### High-Level Architecture

```
┌────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                           │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Dashboard UI (React)                         Port: 3000     │  │
│  │  - Real-time event visualization                             │  │
│  │  - Search and filtering                                      │  │
│  │  - Charts and statistics                                     │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
                                ↓ HTTP GET
┌────────────────────────────────────────────────────────────────────┐
│                          API LAYER                                  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Elastic Query Service                        Port: 8084     │  │
│  │  - REST API endpoints                                        │  │
│  │  - OpenAPI/Swagger documentation                             │  │
│  │  - Pagination and sorting                                    │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
                                ↓ Query
┌────────────────────────────────────────────────────────────────────┐
│                        SEARCH LAYER                                 │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Elasticsearch                                Port: 9200     │  │
│  │  - Full-text search index                                    │  │
│  │  - 3 shards, 1 replica                                       │  │
│  │  - Real-time queries                                         │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
                                ↑ Index
┌────────────────────────────────────────────────────────────────────┐
│                       PROCESSING LAYER                              │
│  ┌─────────────────────┐  ┌─────────────────────┐                  │
│  │ Elasticsearch Svc   │  │ Kafka Streams Svc   │                  │
│  │ Port: 8083          │  │ Port: 8082          │                  │
│  │ - Consume events    │  │ - Stream processing │                  │
│  │ - Transform to docs │  │ - Real-time analytics│                 │
│  │ - Batch index       │  │ - Word counting     │                  │
│  └─────────────────────┘  └─────────────────────┘                  │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Kafka Consumer Service                      Port: 8081      │   │
│  │ - Consume events in batches                                 │   │
│  │ - Calculate metrics and statistics                          │   │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────┘
                                ↑ Subscribe
┌────────────────────────────────────────────────────────────────────┐
│                        EVENT BUS LAYER                              │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Apache Kafka Cluster                                        │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐            │  │
│  │  │ Broker 1   │  │ Broker 2   │  │ Broker 3   │            │  │
│  │  │ :19092     │  │ :29092     │  │ :39092     │            │  │
│  │  └────────────┘  └────────────┘  └────────────┘            │  │
│  │                                                              │  │
│  │  Topics:                                                     │  │
│  │  - social-events (3 partitions, RF=3)                       │  │
│  │  - social-events-filtered                                   │  │
│  │  - social-events-word-count                                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Schema Registry                             Port: 8081     │  │
│  │  - Stores Avro schemas                                       │  │
│  │  - Validates schema compatibility                            │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
                                ↑ Publish
┌────────────────────────────────────────────────────────────────────┐
│                      PRODUCER LAYER                                 │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Event Stream Service                         Port: 8080     │  │
│  │  - Generate mock social media events                         │  │
│  │  - Serialize to Avro format                                  │  │
│  │  - Publish to Kafka                                          │  │
│  │  - 60 events/minute                                          │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│                    COORDINATION LAYER                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Zookeeper                                   Port: 2181      │  │
│  │  - Kafka cluster coordination                                │  │
│  │  - Leader election                                           │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
```

---

## 2. Microservices Breakdown

### Service 1: Event Stream Service

**Port**: 8080
**Language**: Java 21 / Spring Boot 3.2.5

**Responsibility**: Event Generation

**What It Does**:
- Generates realistic social media-style events
- Creates 60 events per minute (configurable)
- Serializes events to Apache Avro format
- Publishes events to Kafka topic `social-events`

**Key Components**:
- `EnhancedMockStreamRunner`: Event generation logic
- `AvroKafkaProducer`: Kafka producer wrapper
- `SocialEventAvroModel`: Avro-generated event model

**Entry Point**:
```
/event-stream-service/src/main/java/.../EventStreamServiceApplication.java
```

**Configuration**:
```yaml
# Key settings
event-stream:
  sleep-ms: 1000              # 1 event per second
  message-count: 60           # Generate 60 messages

kafka:
  topic-name: social-events
  num-partitions: 3
  replication-factor: 3
```

---

### Service 2: Kafka Consumer Service

**Port**: 8081
**Language**: Java 21 / Spring Boot 3.2.5

**Responsibility**: General-Purpose Event Processing

**What It Does**:
- Consumes events from `social-events` topic
- Processes events in batches (500 records)
- Calculates metrics (consumed, processed, failed)
- Logs statistics every 30 seconds
- Uses 3 concurrent consumer threads

**Key Components**:
- `SocialEventKafkaConsumer`: Main consumer logic
- Batch processing with manual acknowledgment
- Micrometer metrics integration

**Consumer Group**: `social-events-consumer-group`

**Entry Point**:
```
/kafka-consumer-service/src/main/java/.../KafkaConsumerServiceApplication.java
```

**Configuration**:
```yaml
kafka-consumer:
  group-id: social-events-consumer-group
  auto-offset-reset: earliest
  batch-listener: true
  max-poll-records: 500
  concurrency: 3               # 3 consumer threads
```

---

### Service 3: Kafka Streams Service

**Port**: 8082
**Language**: Java 21 / Spring Boot 3.2.5

**Responsibility**: Real-Time Stream Processing

**What It Does**:
- Processes event stream in real-time
- Filters events that have text content
- Extracts words and counts frequency (5-minute windows)
- Aggregates events by user
- Produces derived topics

**Key Components**:
- `SocialEventStreamsTopology`: Stream processing topology
- Stateless transformations (filter, map)
- Stateful aggregations (count, windowing)

**Input Topic**: `social-events`
**Output Topics**:
- `social-events-filtered`: Events with text
- `social-events-word-count`: Word frequency analytics

**Application ID**: `social-events-streams-app`

**Entry Point**:
```
/kafka-streams-service/src/main/java/.../KafkaStreamsServiceApplication.java
```

**Stream Processing**:
```
social-events
    ↓ filter (text not null)
social-events-filtered
    ↓ flatMap (extract words)
    ↓ groupBy word
    ↓ window (5-minute tumbling)
    ↓ count
social-events-word-count
```

---

### Service 4: Elasticsearch Service

**Port**: 8083
**Language**: Java 21 / Spring Boot 3.2.5

**Responsibility**: Search Indexing

**What It Does**:
- Consumes events from `social-events` topic
- Transforms Avro models to Elasticsearch documents
- Batch indexes to `social-events-index`
- Creates index with 3 shards, 1 replica
- Enables full-text search on text field

**Key Components**:
- `SocialEventKafkaToElasticConsumer`: Kafka consumer
- `ElasticIndexClient`: Elasticsearch batch indexer
- `SocialEventIndexModel`: Elasticsearch document model

**Consumer Group**: `elasticsearch-consumer-group`

**Entry Point**:
```
/elasticsearch-service/src/main/java/.../ElasticsearchIndexingServiceApplication.java
```

**Transformation**:
```
SocialEventAvroModel           →    SocialEventIndexModel
├── userId: Long               →    ├── userId: Long
├── id: Long                   →    ├── id: String (primary key)
├── text: String               →    ├── text: String (analyzed)
└── createdAt: Long            →    └── createdAt: Date (sortable)
```

**Index Configuration**:
```json
{
  "index": "social-events-index",
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "refresh_interval": "1s"
  }
}
```

---

### Service 5: Elastic Query Service (REST API)

**Port**: 8084
**Language**: Java 21 / Spring Boot 3.2.5

**Responsibility**: Query Interface

**What It Does**:
- Provides HTTP REST API for querying events
- Queries Elasticsearch index
- Supports pagination and sorting
- Full-text search on text field
- Returns JSON responses

**Key Components**:
- `ElasticQueryController`: REST controller
- `ElasticQueryClient`: Elasticsearch query client
- `SocialEventQueryResponseModel`: API response model

**API Endpoints**:
```
GET  /api/v1/events/{id}                  → Get event by ID
GET  /api/v1/events?page=0&size=20        → Get all events (paginated)
GET  /api/v1/events/search?text=kafka     → Full-text search
GET  /api/v1/events/user/{userId}         → Get events by user
```

**Entry Point**:
```
/elastic/elastic-query-service/src/main/java/.../ElasticQueryServiceApplication.java
```

**Documentation**:
- OpenAPI/Swagger UI: http://localhost:8084/swagger-ui.html

---

### Service 6: Dashboard UI

**Port**: 3000
**Framework**: React 18 + Vite + Tailwind CSS

**Responsibility**: Real-Time Visualization

**What It Does**:
- Displays events in real-time (auto-refresh every 5 seconds)
- Full-text search interface
- Event timeline charts (Recharts)
- Statistics dashboard (total events, unique users, rate)
- Service health monitoring
- Responsive design

**Key Components**:
```
src/
├── App.jsx                    → Main application
├── components/
│   ├── EventsList.jsx         → Event table
│   ├── SearchBar.jsx          → Search input
│   ├── EventsChart.jsx        → Timeline chart
│   ├── StatsCard.jsx          → Metric display
│   └── ServiceStatus.jsx      → Health indicators
```

**Technology Stack**:
- **React 18**: Component framework
- **Vite**: Build tool (fast HMR)
- **Tailwind CSS**: Utility-first styling
- **Recharts**: Chart visualization
- **Axios**: HTTP client

**API Integration**:
```javascript
// Polls REST API every 5 seconds
const fetchEvents = async () => {
  const response = await axios.get(
    'http://localhost:8084/api/v1/events?page=0&size=20'
  );
  setEvents(response.data.content);
};

useEffect(() => {
  fetchEvents();
  const interval = setInterval(fetchEvents, 5000);
  return () => clearInterval(interval);
}, []);
```

---

## 3. Data Flow Walkthrough

### End-to-End Event Journey

```
┌─────────────────────────────────────────────────────────────────┐
│ STEP 1: Event Generation                                        │
└─────────────────────────────────────────────────────────────────┘

Event Stream Service (Port 8080)
  ↓
EnhancedMockStreamRunner generates event every 1000ms:
  {
    "userId": 42,
    "id": 1234567890,
    "text": "Learning Spring Boot and Kafka is awesome!",
    "createdAt": 1700500000000
  }
  ↓
Serialize to Avro binary format (schema from Registry)
  ↓
AvroKafkaProducer publishes to topic "social-events"

┌─────────────────────────────────────────────────────────────────┐
│ STEP 2: Kafka Cluster Distribution                              │
└─────────────────────────────────────────────────────────────────┘

Kafka determines partition:
  hash(userId=42) % 3 = Partition 1
  ↓
Event stored in Partition 1 on all 3 brokers (RF=3):
  - Broker 1 (leader)
  - Broker 2 (replica)
  - Broker 3 (replica)
  ↓
Event assigned offset, e.g., 15432

┌─────────────────────────────────────────────────────────────────┐
│ STEP 3: Parallel Consumption (3 Independent Paths)              │
└─────────────────────────────────────────────────────────────────┘

PATH A: General Processing
  ↓
Kafka Consumer Service (Port 8081)
  - Consumer Group: "social-events-consumer-group"
  - Fetches batch of up to 500 events
  - Processes each event
  - Updates metrics (consumed, processed, failed)
  - Logs statistics
  - Commits offset

PATH B: Stream Processing
  ↓
Kafka Streams Service (Port 8082)
  - Application ID: "social-events-streams-app"
  - Reads event from stream
  - Filters: text != null ✓ (passes filter)
  - Publishes to "social-events-filtered"
  - Extracts words: ["learning", "spring", "boot", "kafka", "awesome"]
  - Groups by word, windows by 5 minutes
  - Increments count for each word
  - Publishes to "social-events-word-count"

PATH C: Search Indexing
  ↓
Elasticsearch Service (Port 8083)
  - Consumer Group: "elasticsearch-consumer-group"
  - Fetches event from Kafka
  - Transforms Avro → Elasticsearch document:
      {
        "id": "1234567890",
        "userId": 42,
        "text": "Learning Spring Boot and Kafka is awesome!",
        "createdAt": "2023-11-20T10:00:00Z"
      }
  - Batch indexes to "social-events-index"
  - Document becomes searchable within ~1 second

┌─────────────────────────────────────────────────────────────────┐
│ STEP 4: Query API Layer                                         │
└─────────────────────────────────────────────────────────────────┘

User queries via REST API (Port 8084):
  GET /api/v1/events/search?text=kafka
  ↓
ElasticQueryController receives request
  ↓
ElasticQueryClient queries Elasticsearch:
  {
    "query": {
      "match": {
        "text": "kafka"
      }
    }
  }
  ↓
Elasticsearch returns matching documents
  ↓
Transform to SocialEventQueryResponseModel
  ↓
Return JSON response to client

┌─────────────────────────────────────────────────────────────────┐
│ STEP 5: UI Visualization                                        │
└─────────────────────────────────────────────────────────────────┘

Dashboard UI (Port 3000):
  - Polls API every 5 seconds: GET /api/v1/events
  - Receives JSON array of events
  - Updates React state
  - Re-renders components:
    • EventsList shows event in table
    • EventsChart plots event on timeline
    • StatsCard increments total count
    • ServiceStatus shows all services healthy
  ↓
User sees event in real-time!
```

**Total Latency**: < 5 seconds from generation to searchable and visible in UI.

---

### 🎯 Quick Exercise: Trace an Event's Journey

**Time**: 10 minutes | **Difficulty**: Beginner

**Task**: Draw or write out the complete journey of a single event through the system.

**Event Details**:
```json
{
  "userId": 42,
  "id": 1700500000000,
  "text": "Learning Kafka is awesome!",
  "createdAt": "2024-11-20T10:00:00Z"
}
```

**Questions**:

1. **Step 1**: Which service creates this event?
2. **Step 2**: Which Kafka partition will it go to? (Given userId=42 and 3 partitions)
3. **Step 3**: How many services will consume this event? Name them.
4. **Step 4**: Which service makes it searchable? What index?
5. **Step 5**: How does the Dashboard UI discover this event?

**Bonus**: Estimate the latency at each step.

**Answers**:
<details>
<summary>Click to reveal answers</summary>

**Step 1: Event Creation**
- **Service**: Event Stream Service (Port 8080)
- **Action**: `EnhancedMockStreamRunner` generates event
- **Latency**: ~1ms (in-memory)

**Step 2: Kafka Routing**
- **Partition**: `hash(42) % 3 = 0` → **Partition 0**
- **Topic**: `social-events`
- **Replicas**: Stored on all 3 brokers
- **Latency**: ~10-50ms (network + disk write)

**Step 3: Parallel Consumption** (3 services consume simultaneously)

**Consumer 1**: Kafka Consumer Service (Port 8081)
- Group: `social-events-consumer-group`
- Action: Logs metrics, increments counter
- Latency: ~5ms

**Consumer 2**: Kafka Streams Service (Port 8082)
- Application ID: `social-events-streams-app`
- Action:
  - Filters (text exists ✓)
  - Publishes to `social-events-filtered`
  - Extracts words: ["learning", "kafka", "awesome"]
  - Increments word counts
- Latency: ~20ms

**Consumer 3**: Elasticsearch Service (Port 8083)
- Group: `elasticsearch-consumer-group`
- Action: Transforms and indexes
- Continues to Step 4...

**Step 4: Search Indexing**
- **Service**: Elasticsearch Service (Port 8083)
- **Index**: `social-events-index`
- **Transformation**:
  ```json
  {
    "id": "1700500000000",
    "userId": 42,
    "text": "Learning Kafka is awesome!",
    "createdAt": "2024-11-20T10:00:00Z"
  }
  ```
- **Latency**: ~100-500ms (batch + index refresh)

**Step 5: UI Discovery**
- **Service**: Elastic Query Service (Port 8084)
- **Action**: Dashboard polls `GET /api/v1/events` every 5 seconds
- **Response**: Returns paginated events including our event
- **Dashboard**: React re-renders, shows event in table
- **Latency**: 0-5 seconds (depends on polling interval)

**Total End-to-End Latency**:
```
Event Generation:     1ms
Kafka Write:         50ms
Parallel Processing: 20ms (concurrent)
Elasticsearch Index: 500ms
UI Poll Interval:   2500ms (average)
━━━━━━━━━━━━━━━━━━━━━━━━━
Total:              ~3 seconds (typical)
```

**Key Insight**: Multiple services process the same event in parallel, each for a different purpose!
</details>

---

## 4. Infrastructure Components

### Kafka Cluster

**Configuration**:
- **Brokers**: 3 (ports 19092, 29092, 39092)
- **Zookeeper**: 1 instance (port 2181)
- **Schema Registry**: 1 instance (port 8081)

**Topics**:
| Topic Name | Partitions | Replication Factor | Purpose |
|------------|------------|-------------------|---------|
| `social-events` | 3 | 3 | Main event stream |
| `social-events-filtered` | 3 | 3 | Filtered events (from Streams) |
| `social-events-word-count` | 3 | 3 | Word count analytics |
| `social-events.DLQ` | 1 | 3 | Dead letter queue (failed events) |

**Replication Strategy**:
```
Partition 0:
  Leader:   Broker 1
  Replicas: Broker 2, Broker 3

Partition 1:
  Leader:   Broker 2
  Replicas: Broker 1, Broker 3

Partition 2:
  Leader:   Broker 3
  Replicas: Broker 1, Broker 2
```

Benefits:
- **High Availability**: Survives single broker failure
- **Load Distribution**: Reads/writes spread across brokers
- **Data Durability**: 3 copies of each event

### Elasticsearch Cluster

**Configuration**:
- **Nodes**: 1 (single-node for development)
- **Port**: 9200 (HTTP), 9300 (Transport)
- **Version**: 8.11.0

**Index**: `social-events-index`
- **Shards**: 3 (for parallelism)
- **Replicas**: 1 (for fault tolerance)
- **Refresh Interval**: 1 second

**Kibana**: Port 5601 (visualization and management)

### Schema Registry

**Purpose**: Centralized Avro schema management

**Benefits**:
- **Schema Evolution**: Add fields without breaking consumers
- **Validation**: Reject incompatible schema changes
- **Efficiency**: Schemas stored once, referenced by ID

**Compatibility Modes**:
- **Backward**: New schema can read old data
- **Forward**: Old schema can read new data
- **Full**: Both backward and forward compatible

---

## 5. CQRS Pattern Implementation

### What is CQRS?

**CQRS** = Command Query Responsibility Segregation

**Principle**: Separate read and write operations into different models.

### CQRS in This Project

```
┌─────────────────────────────────────────────────────────────────┐
│                    WRITE SIDE (Command)                          │
└─────────────────────────────────────────────────────────────────┘

Event Stream Service
    ↓ (write command: generate event)
Kafka (append-only log)
    ↓ (write command: index event)
Elasticsearch (write-optimized index)

Optimized for:
  - High throughput writes
  - Event sourcing
  - Append-only operations

┌─────────────────────────────────────────────────────────────────┐
│                     READ SIDE (Query)                            │
└─────────────────────────────────────────────────────────────────┘

Dashboard UI / API Clients
    ↓ (read query: search events)
Elastic Query Service (REST API)
    ↓ (read query: full-text search)
Elasticsearch (read-optimized index)

Optimized for:
  - Fast queries
  - Full-text search
  - Pagination and filtering
  - Aggregations
```

### Benefits in This Project

| Benefit | How It's Achieved |
|---------|-------------------|
| **Independent Scaling** | Scale producers (write) and API (read) separately |
| **Optimized Data Models** | Avro for write, Elasticsearch for read |
| **Flexibility** | Add new read models without affecting writes |
| **Performance** | Write to Kafka (fast append), query Elasticsearch (optimized search) |

---

## 6. Project Structure

### Module Organization

```
spring-boot-microservices-with-kafka-demo/
├── app-config-data/                    # Configuration data classes
│   └── src/main/java/.../config/
│       ├── KafkaConfigData.java
│       ├── KafkaProducerConfigData.java
│       ├── KafkaConsumerConfigData.java
│       ├── ElasticConfigData.java
│       └── ...
│
├── common-config/                      # Shared configuration beans
│   └── src/main/java/.../config/
│       ├── RetryConfig.java
│       └── ...
│
├── common-security/                    # JWT, rate limiting, RBAC
│   └── src/main/java/.../security/
│       ├── JwtTokenProvider.java
│       ├── RateLimitingFilter.java
│       └── SecurityConfig.java
│
├── kafka/                              # Kafka-related modules
│   ├── kafka-model/                    # Avro schema and generated classes
│   │   └── src/main/resources/avro/
│   │       └── social-event.avsc
│   ├── kafka-admin/                    # Topic creation and management
│   ├── kafka-producer/                 # Generic Avro producer
│   └── kafka-consumer/                 # Consumer interface
│
├── elastic/                            # Elasticsearch modules
│   ├── elastic-model/                  # Document models
│   ├── elastic-config/                 # Connection config
│   ├── elastic-index-client/           # Indexing client
│   ├── elastic-query-client/           # Query client
│   └── elastic-query-service/          # REST API service
│
├── event-stream-service/               # Producer microservice
│   └── src/main/java/.../
│       ├── EventStreamServiceApplication.java
│       └── runner/
│           └── EnhancedMockStreamRunner.java
│
├── kafka-consumer-service/             # Consumer microservice
│   └── src/main/java/.../
│       ├── KafkaConsumerServiceApplication.java
│       └── consumer/
│           └── SocialEventKafkaConsumer.java
│
├── kafka-streams-service/              # Streams microservice
│   └── src/main/java/.../
│       ├── KafkaStreamsServiceApplication.java
│       └── topology/
│           └── SocialEventStreamsTopology.java
│
├── elasticsearch-service/              # Indexing microservice
│   └── src/main/java/.../
│       ├── ElasticsearchIndexingServiceApplication.java
│       └── consumer/
│           └── SocialEventKafkaToElasticConsumer.java
│
├── dashboard-ui/                       # React frontend
│   ├── src/
│   │   ├── App.jsx
│   │   └── components/
│   ├── package.json
│   └── vite.config.js
│
├── docker-compose/                     # Infrastructure
│   └── kafka_cluster.yml               # Kafka, Elasticsearch, Zookeeper
│
└── pom.xml                             # Parent Maven POM
```

### Shared Modules Pattern

**Benefits**:
- **Code Reuse**: Common configuration across services
- **Consistency**: Same Kafka/Elasticsearch config everywhere
- **Maintainability**: Change once, apply to all services

**Dependency Flow**:
```
event-stream-service
    ↓ depends on
kafka-producer
    ↓ depends on
kafka-model (Avro schemas)
    ↓ depends on
app-config-data
```

---

## 7. Environment Setup

### Prerequisites Checklist

✅ **Java 21 (LTS)**
```bash
java -version
# Should output: openjdk version "21.0.x" or similar
```

✅ **Maven 3.8+**
```bash
mvn -version
# Should output: Apache Maven 3.8.x or higher
```

✅ **Docker & Docker Compose**
```bash
docker --version
docker-compose --version
# Ensure Docker Desktop is running
```

✅ **Node.js 18+ & npm** (for Dashboard UI)
```bash
node --version  # v18.x or higher
npm --version   # 9.x or higher
```

✅ **RAM**: Minimum 8GB, recommended 16GB
✅ **Disk Space**: 20GB free

### Install Missing Dependencies

**Java 21** (if not installed):
```bash
# macOS (Homebrew)
brew install openjdk@21

# Ubuntu/Debian
sudo apt install openjdk-21-jdk

# Windows: Download from https://adoptium.net/
```

**Maven** (if not installed):
```bash
# macOS
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Windows: Download from https://maven.apache.org/download.cgi
```

**Docker Desktop**:
- macOS/Windows: https://www.docker.com/products/docker-desktop
- Linux: https://docs.docker.com/engine/install/

---

## 8. Running the Project

### Step 1: Clone the Repository

```bash
git clone https://github.com/dsnanayakkara/spring-boot-microservices-with-kafka-demo.git
cd spring-boot-microservices-with-kafka-demo
```

### Step 2: Start Infrastructure

Start Kafka cluster, Elasticsearch, and supporting services:

```bash
cd docker-compose
docker-compose -f kafka_cluster.yml up -d
```

**Services Started**:
- Zookeeper (port 2181)
- Kafka Broker 1 (port 19092)
- Kafka Broker 2 (port 29092)
- Kafka Broker 3 (port 39092)
- Schema Registry (port 8081)
- Elasticsearch (port 9200)
- Kibana (port 5601)

**Wait for Services to Be Ready** (~60 seconds):
```bash
# Watch logs until you see "started" messages
docker-compose -f kafka_cluster.yml logs -f

# Or check individual service health
docker-compose -f kafka_cluster.yml ps
```

**Verify Kafka is Ready**:
```bash
# List topics (should show __consumer_offsets, _schemas, etc.)
docker exec -it kafka-broker-1 kafka-topics --bootstrap-server localhost:9092 --list
```

**Verify Elasticsearch is Ready**:
```bash
curl http://localhost:9200
# Should return JSON with cluster info
```

### Step 3: Build All Microservices

```bash
# Return to project root
cd ..

# Clean and build all modules (skip tests for faster build)
mvn clean install -DskipTests
```

**What This Does**:
- Compiles all Java code
- Generates Avro classes from schemas
- Packages each service as executable JAR
- Installs shared modules to local Maven repo

**Expected Output**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2-3 minutes
```

### Step 4: Run Microservices

Open **5 separate terminal windows** (or tabs) and run each service:

**Terminal 1: Event Stream Service (Producer)**
```bash
cd event-stream-service
mvn spring-boot:run
```
Wait for: `Started EventStreamServiceApplication in X seconds`

**Terminal 2: Kafka Consumer Service**
```bash
cd kafka-consumer-service
mvn spring-boot:run
```
Wait for: `Started KafkaConsumerServiceApplication in X seconds`

**Terminal 3: Kafka Streams Service**
```bash
cd kafka-streams-service
mvn spring-boot:run
```
Wait for: `Started KafkaStreamsServiceApplication in X seconds`

**Terminal 4: Elasticsearch Service**
```bash
cd elasticsearch-service
mvn spring-boot:run
```
Wait for: `Started ElasticsearchIndexingServiceApplication in X seconds`

**Terminal 5: Elastic Query Service (REST API)**
```bash
cd elastic/elastic-query-service
mvn spring-boot:run
```
Wait for: `Started ElasticQueryServiceApplication in X seconds`

### Step 5: Start Dashboard UI

**Terminal 6: React Dashboard**
```bash
cd dashboard-ui
npm install           # First time only
npm run dev
```

**Access Dashboard**: http://localhost:3000

---

### 🎯 Quick Exercise: Startup Health Check

**Time**: 10 minutes | **Difficulty**: Beginner

**Task**: Verify all services are running correctly using command line tools.

**Checklist**:

1. ✅ Check if all Docker containers are running
2. ✅ Verify Kafka topics exist
3. ✅ Confirm events are being produced
4. ✅ Check Elasticsearch index
5. ✅ Test REST API endpoints

**Execute These Commands**:

```bash
# 1. Check Docker containers
docker ps --format "table {{.Names}}\t{{.Status}}"

# How many containers should be running?
# Answer: ___________

# 2. List Kafka topics
docker exec kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# Expected topics: social-events, social-events-filtered, social-events-word-count
# Did you see all three? Yes / No

# 3. Peek at events (first 3)
docker exec kafka-broker-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic social-events \
  --from-beginning \
  --max-messages 3

# Were events displayed? Yes / No

# 4. Check Elasticsearch index
curl http://localhost:9200/_cat/indices?v | grep social-events

# Document count should be growing. Current count: ___________

# 5. Test REST API
curl http://localhost:8084/api/v1/events?size=1 | jq '.content[0]'

# Did you get an event back? Yes / No
```

**Bonus Challenge**:
Access the Dashboard UI at http://localhost:3000 and:
- Count how many events are displayed
- Note the total number of unique users
- Search for "kafka" - how many results?

**Answers**:
<details>
<summary>Click to reveal answers</summary>

**1. Docker Containers**: Should be **7 running**
- zookeeper
- kafka-broker-1
- kafka-broker-2
- kafka-broker-3
- schema-registry
- elasticsearch
- kibana

**2. Kafka Topics**: Should see at least:
- `social-events` (main topic)
- `social-events-filtered` (from Streams)
- `social-events-word-count` (from Streams)
- `__consumer_offsets` (internal Kafka topic)
- `_schemas` (Schema Registry topic)

**3. Events Display**: Yes (you'll see binary Avro data - looks like gibberish but that's normal!)

**4. Elasticsearch Index**:
- Index name: `social-events-index`
- Document count grows ~1/second (60/minute)
- After 5 minutes: ~300 documents

**5. REST API Response**: Should return JSON like:
```json
{
  "content": [{
    "id": "1700500000000",
    "userId": 42,
    "text": "Exploring Spring Boot and Kafka integration today!",
    "createdAt": 1700500000000
  }],
  "totalElements": 123,
  "totalPages": 7
}
```

**If Any Service Fails**:
- Check logs: `docker-compose -f kafka_cluster.yml logs -f <service-name>`
- For Spring Boot services: Check terminal output for stack traces
- Common issues: Port conflicts, insufficient memory, Docker not running

**Success Criteria**: ✅ All checks pass = System is healthy!
</details>

---

## 9. Verification and Testing

### Verify All Services Are Running

**Check Service Health Endpoints**:

```bash
# Event Stream Service
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Kafka Consumer Service
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP"}

# Kafka Streams Service
curl http://localhost:8082/actuator/health
# Expected: {"status":"UP"}

# Elasticsearch Service
curl http://localhost:8083/actuator/health
# Expected: {"status":"UP"}

# Elastic Query Service (REST API)
curl http://localhost:8084/actuator/health
# Expected: {"status":"UP"}
```

### Verify Kafka Topics

```bash
docker exec -it kafka-broker-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

**Expected Topics**:
- `social-events`
- `social-events-filtered`
- `social-events-word-count`

### Verify Events Are Being Produced

```bash
# Consume from the beginning (will show all events)
docker exec -it kafka-broker-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic social-events \
  --from-beginning \
  --max-messages 5
```

You should see Avro binary data (looks like gibberish), which is normal!

### Verify Elasticsearch Index

```bash
# Check if index exists
curl http://localhost:9200/_cat/indices?v

# Should show:
# health status index               docs.count
# green  open   social-events-index 120        ...

# Query some documents
curl http://localhost:9200/social-events-index/_search?size=3 | jq
```

### Test REST API

**Get All Events (Paginated)**:
```bash
curl http://localhost:8084/api/v1/events?page=0&size=5 | jq
```

**Search for Events**:
```bash
curl 'http://localhost:8084/api/v1/events/search?text=kafka' | jq
```

**Get Event by ID** (replace with actual ID from previous query):
```bash
curl http://localhost:8084/api/v1/events/1234567890 | jq
```

### View Dashboard

Open http://localhost:3000 in your browser.

**Expected UI**:
- Events list updating every 5 seconds
- Chart showing event timeline
- Statistics (total events, unique users)
- Service health indicators (all green)
- Search bar (try searching for "kafka")

### View API Documentation

Open http://localhost:8084/swagger-ui.html

**Try It Out**:
- Expand GET /api/v1/events
- Click "Try it out"
- Click "Execute"
- See response

---

## 10. Summary

### What You've Learned

✅ The overall system architecture with 6 microservices
✅ How each service fits into the data flow pipeline
✅ CQRS pattern separating write and read paths
✅ Infrastructure components (Kafka, Elasticsearch, Schema Registry)
✅ How to set up and run the entire project locally
✅ How to verify that all services are working correctly

### System Architecture Recap

```
Event Stream Service → Kafka Cluster → 3 Parallel Consumers:
                                        1. Consumer Service (logs metrics)
                                        2. Streams Service (analytics)
                                        3. Elasticsearch Service (indexes)
                                             ↓
                                        Elasticsearch Index
                                             ↓
                                        Elastic Query Service (REST API)
                                             ↓
                                        Dashboard UI
```

### Key Ports Reference

| Service | Port | URL |
|---------|------|-----|
| Event Stream Service | 8080 | http://localhost:8080/actuator/health |
| Kafka Consumer Service | 8081 | http://localhost:8081/actuator/health |
| Kafka Streams Service | 8082 | http://localhost:8082/actuator/health |
| Elasticsearch Service | 8083 | http://localhost:8083/actuator/health |
| Elastic Query Service | 8084 | http://localhost:8084/api/v1/events |
| Dashboard UI | 3000 | http://localhost:3000 |
| Kafka Broker 1 | 19092 | localhost:19092 |
| Schema Registry | 8081 | http://localhost:8081 |
| Elasticsearch | 9200 | http://localhost:9200 |
| Kibana | 5601 | http://localhost:5601 |

---

## Next Steps

Now that you understand the architecture and have the project running, you're ready to dive deep into event production!

👉 **[Proceed to Module 3: Event Production & Avro Serialization](./03-event-production.md)**

---

## Troubleshooting

### Issue: Docker containers won't start

**Solution**:
```bash
# Check Docker is running
docker ps

# If not, start Docker Desktop

# Check available resources
docker system df

# Free up space if needed
docker system prune -a
```

### Issue: Port already in use

**Solution**:
```bash
# Find process using port 8080 (example)
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml:
server:
  port: 8090
```

### Issue: Kafka broker not reachable

**Solution**:
```bash
# Check broker logs
docker-compose -f kafka_cluster.yml logs kafka-broker-1

# Restart brokers
docker-compose -f kafka_cluster.yml restart

# If still failing, recreate
docker-compose -f kafka_cluster.yml down
docker-compose -f kafka_cluster.yml up -d
```

### Issue: Maven build fails

**Solution**:
```bash
# Clean Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -DskipTests -U
```

### Issue: Elasticsearch index not created

**Solution**:
```bash
# Check Elasticsearch service logs
cd elasticsearch-service
mvn spring-boot:run

# Look for errors related to index creation
# Manually create index:
curl -X PUT http://localhost:9200/social-events-index
```

---

## Exercises

### Exercise 1: Add a New Consumer

**Challenge**: Create a new consumer service that counts events per user and logs the top 10 users.

**Hints**:
- Create new Spring Boot project
- Add `@KafkaListener` with new group ID
- Use `ConcurrentHashMap<Long, AtomicLong>` to track counts
- Log top 10 every 30 seconds

### Exercise 2: Modify Event Schema

**Challenge**: Add a new field `sentiment` (POSITIVE/NEGATIVE/NEUTRAL) to the Avro schema.

**Steps**:
1. Update `social-event.avsc`
2. Regenerate classes: `mvn clean compile`
3. Update producer to set sentiment
4. Update consumers to read sentiment

**Question**: What happens to existing events without the field?

### Exercise 3: Dashboard Feature

**Challenge**: Add a pie chart showing event distribution by hour of day.

**Hints**:
- Extract hour from `createdAt` timestamp
- Use Recharts PieChart component
- Group events by hour
- Update chart on data refresh

---

**Module Progress**: 2 of 10 complete
