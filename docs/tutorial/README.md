# Event-Driven Microservices with Kafka: A Complete Tutorial

## Overview

This comprehensive tutorial teaches you how to build production-ready, event-driven microservices using Spring Boot, Apache Kafka, and Elasticsearch. You'll learn modern distributed systems architecture, stream processing, and real-time data pipelines.

---

## What You'll Learn

### Core Technologies
- **Apache Kafka** - Distributed event streaming platform
- **Spring Boot 3** - Modern Java application framework
- **Kafka Streams** - Real-time stream processing
- **Apache Avro** - Schema-based serialization
- **Elasticsearch** - Full-text search engine
- **React** - Modern frontend framework

### Architectural Patterns
- Event-Driven Architecture (EDA)
- CQRS (Command Query Responsibility Segregation)
- Microservices Communication
- Stream Processing Patterns
- Circuit Breaker & Resilience Patterns

### Engineering Practices
- Schema evolution with Avro
- JWT authentication & authorization
- Rate limiting & security
- Dead letter queues
- Health checks & monitoring
- Docker containerization

---

## Prerequisites

### Required Knowledge
- **Java**: Intermediate level (OOP, generics, annotations, lambdas)
- **Spring Framework**: Basic understanding (dependency injection, beans)
- **REST APIs**: HTTP methods, request/response patterns
- **Databases**: Basic SQL and indexing concepts
- **Command Line**: Terminal/shell navigation
- **Git**: Basic version control operations

### Recommended Knowledge (Helpful but not required)
- Docker and containerization concepts
- Message queuing systems
- Distributed systems basics
- JavaScript/React fundamentals

### Software Requirements
- **Java**: JDK 21 (LTS)
- **Maven**: 3.8+
- **Docker**: Latest version with Docker Compose
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse
- **RAM**: Minimum 8GB (16GB recommended)
- **Disk**: 20GB free space

---

## Tutorial Structure

This tutorial is organized into **10 progressive modules**, each building on previous concepts. Each module contains:

✅ **Theory** - Core concepts and principles
✅ **Architecture** - Design patterns and diagrams
✅ **Implementation** - Detailed code walkthroughs
✅ **Hands-on** - Practical exercises and challenges
✅ **Best Practices** - Production-ready patterns

---

## Learning Path

### 🟢 **Beginner Track** (Modules 1-4)
Start here if you're new to Kafka or event-driven systems.

**[Module 1: Foundational Concepts](./01-foundational-concepts.md)** (60 min)
- What is Event-Driven Architecture?
- Apache Kafka fundamentals
- Topics, partitions, and brokers
- Producers and consumers
- Consumer groups
- Offset management

**[Module 2: Project Architecture & Setup](./02-project-architecture.md)** (45 min)
- System architecture overview
- Microservices breakdown
- Data flow walkthrough
- Setting up the development environment
- Running the project with Docker Compose

**[Module 3: Event Production & Avro Serialization](./03-event-production.md)** (75 min)
- Implementing Kafka producers
- Apache Avro schema definition
- Schema Registry integration
- Event generation patterns
- Graceful shutdown handling

**[Module 4: Event Consumption Patterns](./04-event-consumption.md)** (75 min)
- Implementing Kafka consumers
- Batch processing strategies
- Consumer configuration tuning
- Error handling and retries
- Metrics and monitoring

---

### 🟡 **Intermediate Track** (Modules 5-7)
Build on your foundation with advanced streaming and search.

**[Module 5: Real-Time Stream Processing](./05-stream-processing.md)** (90 min)
- Kafka Streams fundamentals
- Stream topology design
- Stateless transformations (filter, map, flatMap)
- Stateful operations (aggregations, joins)
- Time windowing (tumbling, sliding, session)
- Word count analytics implementation

**[Module 6: Elasticsearch Integration](./06-elasticsearch-integration.md)** (75 min)
- Document-oriented data modeling
- Index creation and configuration
- Batch indexing for throughput
- Avro to Elasticsearch transformation
- Full-text search queries
- Sharding and replication strategies

**[Module 7: REST API Design & Implementation](./07-rest-api-design.md)** (75 min)
- RESTful API principles
- Spring Web MVC architecture
- Pagination and sorting
- Request/response mapping
- OpenAPI/Swagger documentation
- API versioning strategies

---

### 🔴 **Advanced Track** (Modules 8-10)
Master production-ready patterns and deployment.

**[Module 8: Security & Resilience Patterns](./08-security-resilience.md)** (90 min)
- JWT authentication flow
- Role-based access control (RBAC)
- Rate limiting with token bucket algorithm
- Circuit breaker pattern
- Bulkhead isolation
- Dead letter queues
- Graceful degradation

**[Module 9: Frontend Development & Real-Time UI](./09-frontend-development.md)** (60 min)
- React component architecture
- API integration with Axios
- Real-time data updates
- Chart visualization with Recharts
- Error boundary patterns
- Responsive design with Tailwind CSS

**[Module 10: Deployment, Operations & Monitoring](./10-deployment-operations.md)** (75 min)
- Docker Compose orchestration
- Multi-container coordination
- Health checks and readiness probes
- Logging strategies
- Metrics with Spring Actuator
- Production deployment options
- Troubleshooting guide

---

## Hands-On Exercises

Each module includes practical exercises:

- **🧪 Labs**: Step-by-step guided implementations
- **🎯 Challenges**: Open-ended problems to solve
- **🔍 Code Reviews**: Analyze and improve existing code
- **🐛 Debugging**: Fix intentional bugs and issues
- **🚀 Extensions**: Add new features to the system

**[View All Exercises](./exercises/README.md)**

---

## Suggested Learning Sequences

### 📚 **Complete Mastery Path** (12-16 weeks)
- Study 1 module per week
- Complete all exercises
- Build your own variations
- **Time**: 6-10 hours/week

### ⚡ **Fast Track** (2-3 weeks)
- Focus on theory and architecture
- Run existing code
- Light hands-on practice
- **Time**: 15-20 hours/week

### 🎯 **Specialized Paths**

**Path A: Kafka Specialist**
- Modules 1, 3, 4, 5, 8

**Path B: Backend Engineer**
- Modules 1, 2, 3, 4, 6, 7, 8

**Path C: Full-Stack Developer**
- Modules 1, 2, 7, 9, 10

**Path D: DevOps/Platform Engineer**
- Modules 1, 2, 8, 10

---

## How to Use This Tutorial

### 1️⃣ **Read the Theory**
Start each module by understanding the concepts and principles.

### 2️⃣ **Study the Architecture**
Review diagrams and design decisions. Understand the "why" before the "how".

### 3️⃣ **Follow Code Walkthroughs**
Read through implementations with detailed explanations. The code is annotated with line-by-line breakdowns.

### 4️⃣ **Run the Code**
Execute the examples locally. See the system in action.

### 5️⃣ **Complete Exercises**
Apply what you've learned. Start with guided labs, then tackle challenges.

### 6️⃣ **Experiment**
Modify the code. Break things and fix them. This is where deep learning happens.

### 7️⃣ **Review Best Practices**
Understand production considerations and real-world patterns.

---

## Project Quick Start

```bash
# Clone the repository
git clone https://github.com/dsnanayakkara/spring-boot-microservices-with-kafka-demo.git
cd spring-boot-microservices-with-kafka-demo

# Start infrastructure (Kafka, Elasticsearch)
cd docker-compose
docker-compose -f kafka_cluster.yml up -d

# Wait for services to be ready (about 60 seconds)
docker-compose -f kafka_cluster.yml logs -f

# Build all microservices
cd ..
mvn clean install -DskipTests

# Run services (each in separate terminal)
cd event-stream-service && mvn spring-boot:run
cd kafka-consumer-service && mvn spring-boot:run
cd kafka-streams-service && mvn spring-boot:run
cd elasticsearch-service && mvn spring-boot:run
cd elastic/elastic-query-service && mvn spring-boot:run

# Start dashboard
cd dashboard-ui
npm install
npm run dev

# Access the dashboard
open http://localhost:3000
```

---

## Service Endpoints

Once running, access these services:

| Service | URL | Purpose |
|---------|-----|---------|
| **Dashboard UI** | http://localhost:3000 | Real-time event visualization |
| **Event Stream** | http://localhost:8080/actuator/health | Event producer health |
| **Consumer** | http://localhost:8081/actuator/health | Consumer health |
| **Kafka Streams** | http://localhost:8082/actuator/health | Stream processor health |
| **Elasticsearch** | http://localhost:8083/actuator/health | Indexing service health |
| **REST API** | http://localhost:8084/api/v1/events | Query API |
| **API Docs** | http://localhost:8084/swagger-ui.html | Swagger UI |
| **Kibana** | http://localhost:5601 | Elasticsearch visualization |

---

## Additional Resources

### Official Documentation
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kafka Streams Developer Guide](https://kafka.apache.org/documentation/streams/)
- [Elasticsearch Guide](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Apache Avro Documentation](https://avro.apache.org/docs/)

### Recommended Reading
- **Books**:
  - "Designing Data-Intensive Applications" by Martin Kleppmann
  - "Kafka: The Definitive Guide" by Neha Narkhede et al.
  - "Building Microservices" by Sam Newman
  - "Spring Boot in Action" by Craig Walls

- **Online Courses**:
  - Confluent Kafka Fundamentals
  - Spring Boot Microservices (Udemy)
  - Elasticsearch Essentials

### Community
- [Confluent Community](https://forum.confluent.io/)
- [Spring Community Forums](https://spring.io/community)
- [Kafka Users Mailing List](https://kafka.apache.org/contact)

---

## Tutorial Outcomes

By completing this tutorial, you will be able to:

✅ Design and implement event-driven microservices architectures
✅ Build high-throughput Kafka producers and consumers
✅ Create real-time stream processing pipelines with Kafka Streams
✅ Integrate Elasticsearch for full-text search capabilities
✅ Implement security patterns (JWT, rate limiting)
✅ Apply resilience patterns (circuit breakers, retries, DLQs)
✅ Deploy and monitor distributed systems with Docker
✅ Build reactive frontends with real-time data
✅ Make informed architectural decisions for production systems
✅ Debug and troubleshoot distributed applications

---

## Feedback and Contributions

This tutorial is continuously improved based on student feedback. If you find errors, have suggestions, or want to contribute:

- **Issues**: Report problems or unclear sections
- **Pull Requests**: Contribute improvements or new exercises
- **Questions**: Ask in the discussions forum

---

## Let's Get Started!

Ready to begin your journey into event-driven microservices?

👉 **[Start with Module 1: Foundational Concepts](./01-foundational-concepts.md)**

---

## Quick Reference

### Key Concepts by Module

| Module | Key Concepts |
|--------|--------------|
| 1 | Events, Topics, Partitions, Consumer Groups, Offsets |
| 2 | Microservices, CQRS, Data Flow, Service Ports |
| 3 | Producers, Avro, Schema Registry, Serialization |
| 4 | Consumers, Batch Processing, Error Handling |
| 5 | KStreams, KTables, Windowing, Aggregations |
| 6 | Documents, Indices, Shards, Full-Text Search |
| 7 | REST, Pagination, OpenAPI, API Design |
| 8 | JWT, RBAC, Circuit Breakers, Rate Limiting |
| 9 | React, Components, State Management, Real-Time UI |
| 10 | Docker, Health Checks, Metrics, Deployment |

---

**Version**: 1.0
**Last Updated**: November 2024
**Difficulty**: Beginner to Advanced
**Estimated Time**: 12-16 weeks (complete path)

Happy learning! 🚀
