# High-Throughput Kafka Microservices Demo

A Spring Boot microservices demonstration project showcasing event-driven architecture with Apache Kafka, Avro serialization, and high-throughput message processing.

## 🎯 Project Overview

This project demonstrates a production-ready microservices architecture for processing streaming data at high throughput using:

- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.2.5** - Latest generation Spring framework
- **Spring Framework 6.1.x** - Core framework with Jakarta EE 10
- **Apache Kafka** - Distributed event streaming platform
- **Spring Kafka 3.1.x** - Latest Kafka integration
- **Apache Avro** - Efficient data serialization
- **Confluent Platform 7.6.0** - Schema Registry and Kafka tools
- **Spring Boot Actuator** - Production-ready monitoring and health checks

### What Changed from Original Twitter Integration

This project originally integrated with Twitter's streaming API, but due to Twitter's API pricing changes, it has been updated with a **realistic data simulator** that generates social media-like messages for learning and development purposes. This provides:

- ✅ No API credentials required
- ✅ Configurable message generation rate
- ✅ Realistic message patterns and diversity
- ✅ Perfect for learning Kafka microservices
- ✅ High-throughput message generation for load testing

## 📁 Project Structure

```
├── twitter-to-kafka-service/    # Main service that generates and produces messages
├── kafka/
│   ├── kafka-model/             # Avro schema and generated models
│   ├── kafka-admin/             # Kafka cluster administration
│   └── kafka-producer/          # Kafka producer implementation
├── app-config-data/             # Configuration data classes
├── common-config/               # Shared retry and common configurations
└── docker-compose/              # Docker Compose for Kafka cluster
```

## 🚀 Quick Start

### Prerequisites

- **Java 21** (LTS) - [Download here](https://adoptium.net/)
- **Maven 3.9+**
- **Docker** and **Docker Compose**
- At least **4GB RAM** for Docker

### Step 1: Start Kafka Cluster

Navigate to the docker-compose directory and start the Kafka cluster:

```bash
cd docker-compose
docker-compose -f kafka_cluster.yml up -d
```

This will start:
- **Zookeeper** on port 2181
- **3 Kafka Brokers** on ports 19092, 29092, 39092
- **Schema Registry** on port 8081

Wait for all services to be healthy (approximately 1-2 minutes):

```bash
docker-compose -f kafka_cluster.yml ps
```

### Step 2: Build the Application

From the project root:

```bash
mvn clean install
```

### Step 3: Run the Application

```bash
cd twitter-to-kafka-service
mvn spring-boot:run
```

Or run from your IDE (IntelliJ IDEA, Eclipse, etc.) by running:
```
com.microservices.demo.twitter.to.kafka.service.TwitterToKafkaServiceApplication
```

### Step 4: Monitor the Application

Once running, the application will:
1. Initialize Kafka topics automatically
2. Start generating realistic social media messages
3. Send messages to Kafka topic `twitter-topic`

**Health Check Endpoint:**
```bash
curl http://localhost:8080/actuator/health
```

**Metrics Endpoint:**
```bash
curl http://localhost:8080/actuator/metrics
```

**Kafka Health:**
```bash
curl http://localhost:8080/actuator/health/kafka
```

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
twitter-to-kafka-service:
  twitter-keywords: [Java, Microservices, Kafka, Elasticsearch, SpringBoot, Docker, Kubernetes]
  enable-mock-tweets: true
  mock-sleep-ms: 1000  # Generate message every 1 second

kafka-config:
  bootstrap-servers: localhost:19092, localhost:29092, localhost:39092
  schema-registry-url: http://localhost:8081
  topic-name: twitter-topic
  num-of-partitions: 3
  replication-factor: 3
```

### Customizing Message Generation

To change message generation rate, edit `application.yml`:

```yaml
twitter-to-kafka-service:
  mock-sleep-ms: 500  # Generate messages every 500ms (2 per second)
```

To add custom keywords:

```yaml
twitter-to-kafka-service:
  twitter-keywords:
    - YourKeyword1
    - YourKeyword2
```

## 🏗️ Architecture

### Data Flow

```
Enhanced Data Simulator
        ↓
  Generate Message
        ↓
Convert to Avro Model (TwitterAvroModel)
        ↓
   Kafka Producer
        ↓
Kafka Cluster (3 brokers)
        ↓
 Topic: twitter-topic
 (3 partitions, replication factor 3)
```

### Key Components

1. **EnhancedMockStreamRunner**: Generates realistic social media messages at configurable rate
2. **KafkaProducer**: Sends Avro-serialized messages to Kafka
3. **KafkaAdminClient**: Creates topics and manages Kafka infrastructure
4. **TwitterAvroModel**: Schema-based data model for type-safe message handling
5. **RetryTemplate**: Handles transient failures with exponential backoff

## 📈 Monitoring & Observability

### Spring Boot Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-formatted metrics |
| `/actuator/env` | Environment properties |
| `/actuator/loggers` | Logging configuration |

### Production Monitoring

The application exposes Prometheus metrics for integration with monitoring systems:

```bash
curl http://localhost:8080/actuator/prometheus
```

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

### Next Steps

1. Add a **Kafka Consumer** service to process messages
2. Implement **Kafka Streams** for stream processing
3. Add **Elasticsearch** integration for message storage and search
4. Create a **REST API** to query processed data
5. Add **Docker Compose** for the entire application stack

## 🤝 Contributing

This is a learning demonstration project. Feel free to:
- Experiment with different configurations
- Add new message patterns
- Implement consumer services
- Extend with additional microservices

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
