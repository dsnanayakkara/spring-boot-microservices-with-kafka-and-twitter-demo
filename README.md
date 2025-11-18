# High-Throughput Kafka Microservices Demo

A complete Spring Boot microservices architecture demonstrating event-driven patterns with Apache Kafka, real-time stream processing, Elasticsearch search, and RESTful APIs.

## 🎯 Project Overview

This project demonstrates a **production-ready, end-to-end microservices architecture** for processing streaming data at high throughput. The system includes:

- **Event Generation** - Mock social event producer
- **Message Processing** - Kafka consumer with metrics
- **Stream Processing** - Real-time analytics with Kafka Streams
- **Search & Storage** - Elasticsearch indexing and full-text search
- **REST API** - Query interface with OpenAPI documentation
- **Dashboard UI** - Real-time visualization and monitoring interface

### Technology Stack

**Backend:**
- **Java 21** - Latest LTS version with virtual threads support
- **Spring Boot 3.2.5** - Latest generation Spring framework
- **Spring Framework 6.1.x** - Core framework with Jakarta EE 10
- **Apache Kafka** - Distributed event streaming platform (3-broker cluster)
- **Kafka Streams** - Real-time stream processing with windowing
- **Apache Avro 1.11.3** - Efficient data serialization
- **Confluent Platform 7.6.0** - Schema Registry and Kafka tools
- **Elasticsearch 8.11.0** - Full-text search engine
- **Kibana 8.11.0** - Data visualization platform
- **SpringDoc OpenAPI 2.3.0** - API documentation with Swagger UI
- **Spring Boot Actuator** - Production-ready monitoring and health checks

**Frontend:**
- **React 18** - Modern UI framework
- **Vite** - Next-generation build tool
- **Tailwind CSS** - Utility-first CSS framework
- **Recharts** - Composable charting library
- **Axios** - Promise-based HTTP client

### What Changed from Original Twitter Integration

This project originally integrated with Twitter's streaming API, but due to Twitter's API pricing changes, it has been updated with a **realistic data simulator** that generates social media-like messages for learning and development purposes. This provides:

- ✅ No API credentials required
- ✅ Configurable message generation rate (60 events/min)
- ✅ Realistic message patterns and diversity
- ✅ Perfect for learning Kafka microservices
- ✅ High-throughput message generation for load testing
- ✅ Complete end-to-end data pipeline

## 📁 Project Structure

```
├── event-stream-service/        # Event producer service (Port 8080)
├── kafka-consumer-service/      # Kafka consumer with batch processing (Port 8081)
├── kafka-streams-service/       # Stream processing service (Port 8082)
├── elasticsearch-service/       # Elasticsearch indexing service (Port 8083)
├── elastic/
│   ├── elastic-model/           # Elasticsearch domain models
│   ├── elastic-config/          # Elasticsearch configuration
│   ├── elastic-index-client/    # Elasticsearch indexing client
│   ├── elastic-query-client/    # Elasticsearch query client
│   └── elastic-query-service/   # REST API service (Port 8084)
├── kafka/
│   ├── kafka-model/             # Avro schema and generated models
│   ├── kafka-admin/             # Kafka cluster administration
│   ├── kafka-producer/          # Kafka producer implementation
│   └── kafka-consumer/          # Kafka consumer implementation
├── dashboard-ui/                # React dashboard (Port 3000)
│   ├── src/
│   │   ├── components/          # React components
│   │   ├── services/            # API client services
│   │   └── App.jsx              # Main application
│   ├── Dockerfile               # Docker configuration
│   └── package.json             # NPM dependencies
├── app-config-data/             # Configuration data classes
├── common-config/               # Shared retry and common configurations
├── docker-compose/              # Docker Compose for infrastructure
├── start-all-services.sh        # Unified startup script
├── stop-all-services.sh         # Unified shutdown script
└── ARCHITECTURE.md              # Detailed architecture documentation
```

## 🚀 Quick Start

### Prerequisites

- **Java 21** (LTS) - [Download here](https://adoptium.net/)
- **Maven 3.9+**
- **Node.js 18+** and **npm** (for dashboard UI)
- **Docker** and **Docker Compose**
- At least **8GB RAM** for Docker (for all services)

### Option 1: Automated Startup (Recommended)

Use the unified startup script to launch the entire stack:

```bash
./start-all-services.sh
```

This script will:
1. Start infrastructure (Kafka cluster, Elasticsearch, Kibana)
2. Build all services with Maven
3. Start all 5 microservices in the correct order
4. Display service URLs and process IDs

**To stop all services:**
```bash
./stop-all-services.sh
```

### Option 2: Manual Startup

#### Step 1: Start Infrastructure

```bash
cd docker-compose
docker-compose -f kafka_cluster.yml up -d
cd ..
```

This starts:
- **Zookeeper** (port 2181)
- **3 Kafka Brokers** (ports 19092, 29092, 39092)
- **Schema Registry** (port 8081)
- **Elasticsearch** (port 9200)
- **Kibana** (port 5601)

Wait 60 seconds for infrastructure to be ready.

#### Step 2: Build All Services

```bash
mvn clean install -DskipTests
```

#### Step 3: Start Services

Open 5 terminal windows and run each service:

**Terminal 1 - Event Stream Service:**
```bash
cd event-stream-service
mvn spring-boot:run
```

**Terminal 2 - Kafka Consumer Service:**
```bash
cd kafka-consumer-service
mvn spring-boot:run
```

**Terminal 3 - Kafka Streams Service:**
```bash
cd kafka-streams-service
mvn spring-boot:run
```

**Terminal 4 - Elasticsearch Service:**
```bash
cd elasticsearch-service
mvn spring-boot:run
```

**Terminal 5 - REST API Service:**
```bash
cd elastic/elastic-query-service
mvn spring-boot:run
```

### Verify Services are Running

Check health endpoints:

```bash
# Event Stream Service
curl http://localhost:8080/actuator/health

# Kafka Consumer Service
curl http://localhost:8081/actuator/health

# Kafka Streams Service
curl http://localhost:8082/actuator/health

# Elasticsearch Service
curl http://localhost:8083/actuator/health

# REST API Service
curl http://localhost:8084/actuator/health
```

### Access the API

**Swagger UI (Interactive API Documentation):**
```
http://localhost:8084/swagger-ui.html
```

**Sample API Requests:**

```bash
# Get all events (paginated)
curl "http://localhost:8084/api/v1/events?page=0&size=10"

# Search events by text
curl "http://localhost:8084/api/v1/events/search?text=kafka&page=0&size=10"

# Get event by ID
curl "http://localhost:8084/api/v1/events/{event-id}"

# Get events by user ID
curl "http://localhost:8084/api/v1/events/user/12345"
```

### Access Kibana (Data Visualization)

```
http://localhost:5601
```

Navigate to "Discover" and create an index pattern for `social-events-index` to visualize the data.

### Access the Dashboard UI

**Start the Dashboard:**

```bash
cd dashboard-ui
npm install
npm run dev
```

**Access the Dashboard:**
```
http://localhost:3000
```

**Dashboard Features:**
- **Real-time Event Monitoring** - Auto-refresh every 5 seconds
- **Event Search** - Full-text search across all events
- **Interactive Charts** - Visualize events over time
- **Service Health Monitoring** - Real-time status of all microservices
- **Statistics Dashboard** - Total events, unique users, events per minute
- **Responsive Design** - Works on desktop, tablet, and mobile

See [dashboard-ui/README.md](dashboard-ui/README.md) for more details.

## 📊 Message Generation

The enhanced data simulator generates realistic social media messages with:

- **Categories**: Tech discussions, tutorials, questions, announcements
- **Keywords**: Java, Microservices, Kafka, Elasticsearch, SpringBoot, Docker, Kubernetes
- **Frequency**: Configurable (default: 1 message per second)
- **Diversity**: Multiple message templates for realistic variation

### Sample Generated Messages

```
"Just deployed a new microservice using Kafka! The performance improvements are incredible. #DevOps #CloudNative"

"Learning Microservices has been a game changer for our architecture. Highly recommend checking it out! #TechTips"

"New tutorial: Getting started with SpringBoot in 10 minutes. Perfect for beginners!"
```

## ⚙️ Configuration

### Key Configuration Files

**application.yml** - Main application configuration
```yaml
event-stream-service:
  event-keywords: [Java, Microservices, Kafka, Elasticsearch, SpringBoot, Docker, Kubernetes]
  enable-mock-events: true
  mock-sleep-ms: 1000  # Generate event every 1 second

kafka-config:
  bootstrap-servers: localhost:19092, localhost:29092, localhost:39092
  schema-registry-url: http://localhost:8081
  topic-name: social-events
  num-of-partitions: 3
  replication-factor: 3
```

### Customizing Event Generation

To change event generation rate, edit `application.yml`:

```yaml
event-stream-service:
  mock-sleep-ms: 500  # Generate events every 500ms (2 per second)
```

To add custom keywords:

```yaml
event-stream-service:
  event-keywords:
    - YourKeyword1
    - YourKeyword2
```

## 🏗️ System Architecture

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

### High-Level Data Flow

```
Event Stream Service (Port 8080)
    ↓ Generates 60 events/min
    ↓ Avro serialization
    ↓
Kafka Cluster (3 Brokers: 19092, 29092, 39092)
    ├── Topic: social-events (3 partitions, RF=3)
    │
    ├─→ Consumer Service (Port 8081)
    │   └─→ Batch processing (500 records/poll)
    │       └─→ Metrics tracking
    │
    ├─→ Streams Service (Port 8082)
    │   └─→ Real-time processing
    │       ├─→ Word count (5-min windows)
    │       └─→ Filtered events
    │
    └─→ Elasticsearch Service (Port 8083)
        └─→ Indexes to Elasticsearch
            └─→ Full-text search enabled
                └─→ REST API Service (Port 8084)
                    └─→ Swagger UI + API endpoints
```

### Services Overview

#### 1. Event Stream Service (Port 8080)
- **Purpose**: Generates mock social events and publishes to Kafka
- **Rate**: 60 events per minute (configurable)
- **Serialization**: Apache Avro
- **Key Features**:
  - Realistic event generation with diverse content
  - Automatic topic creation
  - Health checks and metrics

#### 2. Kafka Consumer Service (Port 8081)
- **Purpose**: General-purpose event consumer with batch processing
- **Consumer Group**: `social-events-consumer-group`
- **Key Features**:
  - Batch processing (500 records per poll)
  - 3 concurrent consumer threads
  - Micrometer metrics (consumed, processed, failed counts)
  - Processing time tracking

#### 3. Kafka Streams Service (Port 8082)
- **Purpose**: Real-time stream processing and analytics
- **Application ID**: `social-events-streams-app`
- **Key Features**:
  - Event filtering (events with text content)
  - Word extraction and stop word removal
  - Word count with 5-minute tumbling windows
  - User event count aggregation
  - 2 stream processing threads
  - Output to derived topics

#### 4. Elasticsearch Service (Port 8083)
- **Purpose**: Indexes events to Elasticsearch for search
- **Consumer Group**: `elasticsearch-consumer-group`
- **Index**: `social-events-index` (3 shards, 1 replica)
- **Key Features**:
  - Avro to Elasticsearch transformation
  - Batch indexing for efficiency
  - Automatic index creation with mappings
  - Full-text search on event text

#### 5. REST API Service (Port 8084)
- **Purpose**: Query interface for indexed events
- **Base Path**: `/api/v1/events`
- **Key Features**:
  - **GET** `/api/v1/events/{id}` - Get event by ID
  - **GET** `/api/v1/events` - Get all events (paginated)
  - **GET** `/api/v1/events/search?text={text}` - Full-text search
  - **GET** `/api/v1/events/user/{userId}` - Get events by user
  - Pagination and sorting support
  - OpenAPI 3.0 documentation
  - Swagger UI at `/swagger-ui.html`

### Infrastructure Components

| Component | Port | Purpose |
|-----------|------|---------|
| Zookeeper | 2181 | Kafka coordination |
| Kafka Broker 1 | 19092 | Message broker |
| Kafka Broker 2 | 29092 | Message broker |
| Kafka Broker 3 | 39092 | Message broker |
| Schema Registry | 8081 | Avro schema management |
| Elasticsearch | 9200 | Search engine |
| Kibana | 5601 | Data visualization |

### Key Design Patterns

1. **Event-Driven Architecture**
   - Asynchronous message-driven communication
   - Loose coupling between services
   - Event sourcing for audit trail

2. **CQRS (Command Query Responsibility Segregation)**
   - Write path: Kafka → Elasticsearch (indexing)
   - Read path: REST API → Elasticsearch (queries)
   - Separate models for reads and writes

3. **Stream Processing**
   - Real-time analytics with Kafka Streams
   - Stateful aggregations
   - Time-windowed operations

4. **Batch Processing**
   - Efficient high-throughput consumption
   - Configurable batch sizes
   - Parallel processing

### Scalability Features

- **Kafka**: 3 brokers, 3 partitions per topic, replication factor 3
- **Elasticsearch**: 3 shards, 1 replica for fault tolerance
- **Consumers**: Concurrent threads (3 per service)
- **Stateless Services**: Horizontal scaling ready

## 📈 Monitoring & Observability

### Spring Boot Actuator Endpoints

All services expose the following actuator endpoints:

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-formatted metrics |
| `/actuator/env` | Environment properties |
| `/actuator/loggers` | Logging configuration |

### Service Health Checks

```bash
# Event Stream Service
curl http://localhost:8080/actuator/health

# Kafka Consumer Service
curl http://localhost:8081/actuator/health

# Kafka Streams Service
curl http://localhost:8082/actuator/health

# Elasticsearch Service
curl http://localhost:8083/actuator/health

# REST API Service
curl http://localhost:8084/actuator/health
```

### Production Monitoring

All services expose Prometheus metrics for integration with monitoring systems:

```bash
# Example: Event Stream Service metrics
curl http://localhost:8080/actuator/prometheus

# Consumer Service metrics (includes custom consumer metrics)
curl http://localhost:8081/actuator/prometheus
```

### Custom Metrics

The **Kafka Consumer Service** provides custom metrics:
- `kafka.consumer.messages.consumed` - Total messages consumed
- `kafka.consumer.messages.processed` - Successfully processed messages
- `kafka.consumer.messages.failed` - Failed messages
- `kafka.consumer.processing.time` - Processing time distribution

### Kibana Dashboards

Access Kibana at `http://localhost:5601` to:
- Create visualizations of indexed events
- Build custom dashboards
- Analyze event patterns and trends
- Monitor indexing performance

## 🛠️ Development

### Running Tests

```bash
mvn test
```

### Building Docker Image

```bash
mvn spring-boot:build-image
```

### Debugging

Set log level to DEBUG in `application.yml`:

```yaml
logging:
  level:
    com.microservices.demo: DEBUG
```

## 🔧 Troubleshooting

### Kafka Cluster Not Starting

Check Docker resources:
```bash
docker stats
```

Ensure at least 4GB RAM is allocated to Docker.

### Schema Registry Connection Issues

Verify Schema Registry is running:
```bash
curl http://localhost:8081/
```

### Application Can't Connect to Kafka

Check Kafka broker health:
```bash
docker-compose -f docker-compose/kafka_cluster.yml ps
```

Verify all brokers are healthy and ports are accessible.

### Topics Not Created

The application automatically creates topics on startup. Check logs:
```bash
# Look for "Kafka topics created successfully" in logs
```

## 📚 Learning Resources

### Key Concepts Demonstrated

- **Event-Driven Architecture**: Asynchronous message processing
- **Avro Serialization**: Efficient, schema-based serialization
- **Kafka Producer Patterns**: Async sending with callbacks
- **Retry Mechanisms**: Exponential backoff for resilience
- **Health Checks**: Production-ready monitoring
- **Multi-Broker Setup**: High availability configuration

### Implemented Features

✅ **Phase 1: Kafka Consumer Service** - Batch processing with metrics tracking
✅ **Phase 2: Kafka Streams Service** - Real-time stream processing and analytics
✅ **Phase 3: Elasticsearch Integration** - Full-text search and indexing
✅ **Phase 4: REST API Service** - Query interface with OpenAPI documentation
✅ **Phase 5: Dashboard UI** - Real-time event visualization and monitoring

### Future Enhancements

The following features are planned for future releases:

**Additional Improvements:**
- Authentication & Authorization (OAuth 2.0 / JWT)
- Rate limiting on REST API
- Caching layer (Redis)
- Message encryption for sensitive data
- Dead letter queues for failed messages
- Circuit breakers for resilience
- Distributed tracing (OpenTelemetry)
- Container orchestration (Kubernetes manifests)

## 📝 Logs

Service logs are written to the `./logs` directory when using the unified startup script:

- `event-stream-service.log`
- `kafka-consumer-service.log`
- `kafka-streams-service.log`
- `elasticsearch-service.log`
- `elastic-query-service.log`

Monitor logs in real-time:
```bash
tail -f logs/event-stream-service.log
tail -f logs/kafka-consumer-service.log
```

## 🤝 Contributing

This is a learning demonstration project. Feel free to:
- Experiment with different configurations
- Add new message patterns
- Extend the stream processing topology
- Add new REST API endpoints
- Implement additional microservices
- Create custom Kibana dashboards

## 📝 License

This project is for educational purposes.

## 🔗 Related Technologies

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Kafka](https://kafka.apache.org/)
- [Apache Avro](https://avro.apache.org/)
- [Confluent Platform](https://www.confluent.io/)
- [Spring Kafka](https://spring.io/projects/spring-kafka)

---

**Note**: This project has been updated to be development-server ready, removing the dependency on Twitter's API and replacing it with a realistic data simulator for learning purposes.
