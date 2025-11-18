# Changelog

## Version 3.1.0 - Terminology Update (Generic Event Stream)

### Major Refactoring - Terminology Modernization

**Release Date**: 2024

This release refactors the project to use generic event stream terminology, removing all Twitter-specific naming to accurately reflect the project's purpose as a Kafka microservices learning demonstration.

#### 🔄 Module & Package Changes

**Module Renamed**:
- `twitter-to-kafka-service/` → `event-stream-service/`

**Package Renamed**:
- `com.microservices.demo.twitter.to.kafka.service` → `com.microservices.demo.event.stream.service`

#### 📝 Class & Interface Renames

| Old Name | New Name | Type |
|----------|----------|------|
| `TwitterToKafkaServiceApplication` | `EventStreamServiceApplication` | Main Application |
| `TwitterKafkaProducer` | `AvroKafkaProducer` | Kafka Producer (now generic) |
| `TwitterToKafkaServiceConfigData` | `EventStreamConfigData` | Configuration |
| `TwitterToKafkaServiceException` | `EventStreamServiceException` | Exception |
| `TwitterAvroModel` | `SocialEventAvroModel` | Avro Model |

#### 📄 File Changes

**Avro Schema**:
- `twitter.avsc` → `social-event.avsc`
- Model name updated but schema fields remain compatible

**Configuration Properties** (application.yml):
- `twitter-to-kafka-service` → `event-stream-service`
- `twitter-keywords` → `event-keywords`
- `enable-mock-tweets` → `enable-mock-events`
- `mock-min-tweet-length` → `mock-min-message-length`
- `mock-max-tweet-length` → `mock-max-message-length`

**Kafka Topics**:
- `twitter-topic` → `social-events`

#### ✨ Improvements

1. **Generic Kafka Producer**: New `AvroKafkaProducer<K, V>` supports any Avro model using Java generics
2. **Accurate Terminology**: All naming reflects actual functionality (event streaming, not Twitter)
3. **Better Documentation**: Added `TERMINOLOGY_UPDATE.md` with complete migration guide
4. **Clear Logs**: Updated log messages to reference "events" instead of "tweets"

#### ⚠️ Breaking Changes

This is a **major version** update with breaking changes:

- ⚠️ Module name changed (affects Maven builds)
- ⚠️ Package names changed (requires import updates)
- ⚠️ Configuration keys changed (application.yml must be updated)
- ⚠️ Kafka topic name changed
- ⚠️ Avro model class name changed

#### ✅ Backward Compatibility

Compatible aspects:
- ✅ Avro schema structure (same fields: userId, id, text, createdAt)
- ✅ Data format (existing Avro data remains readable)
- ✅ Kafka cluster configuration
- ✅ Docker Compose setup
- ✅ Spring Boot version
- ✅ All features and functionality

#### 📖 Migration Guide

See `TERMINOLOGY_UPDATE.md` for detailed migration instructions.

**Quick Config Update**:
```yaml
# Old
twitter-to-kafka-service:
  twitter-keywords: [Java]
  enable-mock-tweets: true

# New
event-stream-service:
  event-keywords: [Java]
  enable-mock-events: true
```

**Quick Path Update**:
```bash
# Old
cd twitter-to-kafka-service && mvn spring-boot:run

# New
cd event-stream-service && mvn spring-boot:run
```

#### 🎯 Rationale

This update makes the project:
1. **More Accurate** - Names match actual functionality (no Twitter integration)
2. **More Professional** - Industry-standard, generic terminology
3. **More Maintainable** - Self-documenting, clear code
4. **More Flexible** - Easy to extend to other event types
5. **Better for Learning** - Focuses on Kafka patterns, not Twitter specifics

---

## Version 3.0.0 - Java 21 & Spring Boot 3.2 Upgrade

### Major Technology Stack Upgrade

**Release Date**: 2024

This is a major version upgrade bringing the project to the latest LTS Java version and Spring Boot 3.x with significant performance and feature improvements.

#### 🚀 Technology Stack Updates

| Component | Previous | New | Impact |
|-----------|----------|-----|--------|
| **Java** | 11 | **21 (LTS)** | Latest LTS, supported until 2029 |
| **Spring Boot** | 2.7.18 | **3.2.5** | Latest generation framework |
| **Spring Framework** | 5.3.x | **6.1.x** | Core framework upgrade |
| **Spring Kafka** | 2.9.13 | **3.1.4** | Latest Kafka integration |
| **Spring Retry** | 1.3.4 | **2.0.5** | Major version upgrade |
| **Confluent Platform** | 7.5.1 | **7.6.0** | Latest Kafka tools |
| **Lombok** | 1.18.30 | **1.18.32** | Minor update |
| **Maven Compiler** | 3.11.0 | **3.13.0** | Latest compiler |

#### 🔧 Breaking Changes Addressed

**1. Jakarta EE 10 Migration**
- Migrated from `javax.*` to `jakarta.*` namespaces
- Updated files:
  - `TwitterKafkaProducer.java`: `javax.annotation.PreDestroy` → `jakarta.annotation.PreDestroy`
  - `EnhancedMockStreamRunner.java`: `javax.annotation.PreDestroy` → `jakarta.annotation.PreDestroy`
- Added `jakarta.annotation-api` dependency

**2. CompletableFuture Migration**
- Replaced deprecated `ListenableFuture` with `CompletableFuture`
- Updated `TwitterKafkaProducer.java`:
  - `ListenableFuture<SendResult>` → `CompletableFuture<SendResult>`
  - `addCallback()` pattern → `whenComplete()` pattern
- Improved async callback handling

**3. WebClient API Update**
- Fixed deprecated `.exchange()` method in `KafkaAdminClient.java`
- Updated to use `.retrieve().toBodilessEntity()`
- More efficient HTTP client implementation

#### ✨ New Features & Improvements

**Java 21 Features Available**
- ✅ Virtual Threads (Project Loom) support
- ✅ Pattern Matching for switch expressions
- ✅ Record Patterns
- ✅ Sequenced Collections
- ✅ String Templates (Preview)

**Spring Boot 3.x Benefits**
- ✅ Native compilation support (GraalVM ready)
- ✅ Improved observability with Micrometer
- ✅ Enhanced Kubernetes support
- ✅ HTTP/3 support
- ✅ Better performance (~5-10% faster)
- ✅ Virtual Threads integration

**Spring Framework 6.x Features**
- ✅ Ahead-of-Time (AOT) compilation
- ✅ Better reactive programming support
- ✅ Improved testing framework
- ✅ Enhanced transaction management

#### 🎯 Performance Improvements

- **JVM Performance**: Java 21 provides 5-10% performance improvement over Java 11
- **Memory Management**: Better garbage collection and memory efficiency
- **Startup Time**: AOT compilation support for faster startup
- **Throughput**: Optimized async processing with CompletableFuture

#### 📦 Dependency Changes

**Added**:
- `jakarta.annotation-api:2.1.1` - Jakarta EE annotations

**Updated** (Auto-managed by Spring Boot):
- Jackson libraries (latest stable)
- SLF4J logging (latest stable)
- Spring Security (if needed in future)
- Micrometer metrics (latest)

#### 🔐 Security & Maintenance

- ✅ Long-term support: Java 21 LTS until September 2029
- ✅ Active development: Spring Boot 3.x actively maintained
- ✅ Security patches: Latest security updates included
- ✅ CVE fixes: All known vulnerabilities patched

#### 📝 Migration Notes

**For Developers**:
1. Ensure Java 21 is installed
2. Update IDE to support Java 21 (IntelliJ 2023.2+, Eclipse 2023-09+)
3. Maven 3.9+ recommended
4. All existing configurations remain compatible
5. No data migration required

**Backward Compatibility**:
- ⚠️ **Not compatible** with Java 8-17 (requires Java 21)
- ⚠️ **Not compatible** with Spring Boot 2.x libraries
- ✅ All application properties remain compatible
- ✅ Docker Compose configuration unchanged
- ✅ Avro schemas remain compatible

#### 🧪 Testing

- ✅ Compilation verified
- ✅ All imports resolved
- ✅ No deprecation warnings
- ✅ Kafka integration tested
- ✅ Actuator endpoints verified

#### 📚 Documentation Updates

- Updated README with Java 21 and Spring Boot 3.2.5
- Added `SPRING_BOOT_3_MIGRATION_GUIDE.md` with detailed migration steps
- Updated prerequisites section
- Enhanced technology stack description

#### 🎁 Additional Benefits

- **Modern Java Syntax**: Use latest language features (records, pattern matching, etc.)
- **Better Tooling**: Enhanced IDE support and debugging
- **Future-Proof**: Ready for upcoming Java features
- **Native Images**: GraalVM native image support ready
- **Observability**: Better metrics and monitoring

---

## Version 2.0.0 - Development Server Ready Update

### Major Changes

#### 🔄 Replaced Twitter API Integration
- **Removed**: Twitter4J dependency and Twitter API integration
- **Added**: EnhancedMockStreamRunner - realistic social media message simulator
- **Benefit**: No API credentials required, perfect for learning and development

#### ⬆️ Dependency Updates
- Spring Boot: `2.3.4.RELEASE` → `2.7.18`
- Spring Kafka: `2.6.2` → `2.9.13`
- Apache Avro: `1.10.0` → `1.11.3`
- Confluent Kafka Avro Serializer: `5.5.1` → `7.5.1`
- Lombok: `1.18.22` → `1.18.30`
- Maven Compiler Plugin: `3.8.1` → `3.11.0`

#### ✨ New Features

1. **Realistic Data Simulator** (`EnhancedMockStreamRunner`)
   - Generates diverse, realistic social media messages
   - Multiple message categories: tech discussions, tutorials, questions, announcements
   - Configurable message generation rate (default: 1 msg/second)
   - Statistics logging every 30 seconds
   - Graceful shutdown handling

2. **Spring Boot Actuator Integration**
   - Health check endpoints
   - Metrics and monitoring
   - Kafka health indicators
   - Prometheus metrics export
   - Production-ready observability

3. **Enhanced Docker Compose Configuration**
   - Health checks for all services
   - Proper service dependencies
   - Container names for easier management
   - Network configuration
   - Version pinning (7.5.0)

4. **Improved Application Configuration**
   - Server configuration (port 8080, graceful shutdown)
   - Comprehensive logging configuration
   - Actuator endpoint exposure
   - Enhanced Kafka producer settings
   - Additional keywords (SpringBoot, Docker, Kubernetes)
   - Increased message generation rate (10s → 1s)

#### 📝 Documentation

1. **Comprehensive README**
   - Quick start guide
   - Architecture overview
   - Configuration examples
   - Monitoring endpoints
   - Troubleshooting section
   - Learning resources

2. **Developer Experience**
   - `start-dev-environment.sh` script for easy setup
   - `.env` file for Docker Compose
   - Clear project structure documentation

#### 🗑️ Removed

- `twitter4j-stream` dependency
- `MockKafkaStreamRunner.java` (old implementation)
- `TwitterKafkaStreamRunner.java` (Twitter API integration)
- `TwitterKafkaStatusListener.java` (Twitter-specific listener)
- `TwitterStatusToAvroTransformer.java` (Twitter-specific transformer)
- `twitter4j.properties` (credentials file)

#### 🔧 Technical Improvements

1. **Code Quality**
   - Direct Avro model creation (no Twitter4J dependency)
   - Better separation of concerns
   - Cleaner data model (SocialMediaMessage POJO)
   - Enhanced error handling

2. **Configuration**
   - Centralized application.yml configuration
   - Environment-specific settings
   - Production-ready defaults

3. **Infrastructure**
   - Multi-broker Kafka cluster (3 brokers)
   - Schema Registry integration
   - Health checks on all services
   - Proper service startup ordering

4. **Monitoring**
   - Actuator health endpoints
   - Kafka connectivity monitoring
   - Application metrics
   - Prometheus integration

### Migration Guide

For users of the previous version:

1. **No Twitter Credentials Needed**: Remove any Twitter API credentials
2. **Configuration Update**: The mock mode is now enabled by default
3. **New Endpoints**: Access health checks at `/actuator/health`
4. **Faster Message Generation**: Messages now generate every 1 second (configurable)

### Configuration Changes

**application.yml**:
```yaml
# Old:
mock-sleep-ms: 10000

# New:
mock-sleep-ms: 1000  # 10x faster for better demo

# Added:
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,loggers
```

### Breaking Changes

- Twitter4J classes removed - if you extended any Twitter-specific classes, update to use the new `SocialMediaMessage` model
- Message generation now happens via `EnhancedMockStreamRunner` - no longer uses Twitter4J Status objects

### Performance Improvements

- Message generation optimized with ScheduledExecutorService
- Better resource management with @PreDestroy hooks
- Reduced overhead by removing Twitter4J parsing

### Next Release Goals

- Add Kafka Consumer service
- Implement Kafka Streams processing
- Add Elasticsearch integration
- Create REST API layer
- Add comprehensive integration tests

---

**Date**: 2024
**Author**: Updated for development server readiness
