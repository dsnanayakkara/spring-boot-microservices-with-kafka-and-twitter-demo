# Module 3: Event Production & Avro Serialization

**Duration**: 75 minutes
**Difficulty**: Intermediate
**Prerequisites**: Modules 1-2 completed

---

## Learning Objectives

By the end of this module, you will understand:

✅ How to implement a Kafka producer in Spring Boot
✅ Apache Avro schema definition and benefits
✅ Schema Registry integration and version management
✅ Producer configuration and tuning
✅ Callback handling for asynchronous sends
✅ Error handling and retry mechanisms

---

## Table of Contents

1. [Apache Avro Overview](#1-apache-avro-overview)
2. [Defining Avro Schemas](#2-defining-avro-schemas)
3. [Schema Registry Integration](#3-schema-registry-integration)
4. [Implementing Kafka Producers](#4-implementing-kafka-producers)
5. [Event Generation Logic](#5-event-generation-logic)
6. [Producer Configuration](#6-producer-configuration)
7. [Error Handling](#7-error-handling)
8. [Performance Tuning](#8-performance-tuning)
9. [Summary](#9-summary)

---

## 1. Apache Avro Overview

### What is Apache Avro?

**Apache Avro** is a data serialization system that provides:
- **Compact binary format**: Smaller than JSON/XML
- **Schema-based**: Data structure is defined upfront
- **Schema evolution**: Add/remove fields without breaking compatibility
- **Language-agnostic**: Works with Java, Python, C++, etc.

### Why Avro for Kafka?

| Format | Size | Schema | Evolution | Speed |
|--------|------|--------|-----------|-------|
| **JSON** | Large | No | Manual | Slow |
| **Avro** | Small | Yes | Built-in | Fast |
| **Protobuf** | Small | Yes | Yes | Fast |

**Avro Advantages**:
- **Self-describing**: Schema stored with data
- **Compact**: Binary encoding reduces storage/bandwidth
- **Schema evolution**: Backward/forward compatibility
- **Code generation**: Automatic POJO creation

### Avro vs JSON Example

**JSON (73 bytes)**:
```json
{"userId":42,"id":1234567890,"text":"Hello Kafka","createdAt":1700500000000}
```

**Avro (binary, ~40 bytes)**:
```
\x54\x92\xf5\x94\x9a\x0b\x16Hello Kafka\x80\xe0\xe4\x8e\xf2\x62
```

**Size Reduction**: ~45% smaller!

---

## 2. Defining Avro Schemas

### Schema Location

```
kafka/kafka-model/src/main/resources/avro/social-event.avsc
```

### Social Event Schema

```json
{
  "namespace": "com.learning.kafka.avro.model",
  "type": "record",
  "name": "SocialEventAvroModel",
  "doc": "Represents a social media event (post, tweet, message, etc.)",
  "fields": [
    {
      "name": "userId",
      "type": "long",
      "doc": "Unique identifier of the user who created the event"
    },
    {
      "name": "id",
      "type": "long",
      "doc": "Unique identifier of the event"
    },
    {
      "name": "text",
      "type": ["null", "string"],
      "default": null,
      "doc": "Text content of the event (optional)"
    },
    {
      "name": "createdAt",
      "type": "long",
      "logicalType": "timestamp-millis",
      "doc": "Timestamp when the event was created (milliseconds since epoch)"
    }
  ]
}
```

### Schema Field Types

**Primitive Types**:
- `null`, `boolean`, `int`, `long`, `float`, `double`, `bytes`, `string`

**Complex Types**:
- `record`: Structured data (like a class)
- `enum`: Enumeration
- `array`: List of values
- `map`: Key-value pairs
- `union`: Multiple possible types (e.g., `["null", "string"]` = optional string)

**Logical Types**:
- `timestamp-millis`: Long representing milliseconds since epoch
- `date`: Int representing days since epoch
- `decimal`: Arbitrary precision numbers

### Union Types (Optional Fields)

```json
{
  "name": "text",
  "type": ["null", "string"],  // Can be null OR string
  "default": null               // Default value if not provided
}
```

**In Java**:
```java
SocialEventAvroModel event = new SocialEventAvroModel();
event.setText(null);              // Valid
event.setText("Hello");           // Valid
```

---

## 3. Schema Registry Integration

### What is Schema Registry?

**Confluent Schema Registry** is a centralized service that:
- Stores Avro schemas
- Assigns unique IDs to schemas
- Validates schema compatibility
- Enables schema evolution

### How It Works

```
┌───────────────────────────────────────────────────────────┐
│ Producer                                                   │
│   ↓                                                        │
│ 1. Look up schema in Registry                             │
│    - If exists: Get schema ID                             │
│    - If new: Register schema, get ID                      │
│   ↓                                                        │
│ 2. Serialize data with Avro                               │
│   ↓                                                        │
│ 3. Prepend schema ID to binary data                       │
│    [Schema ID: 5 bytes][Avro Data: N bytes]               │
│   ↓                                                        │
│ 4. Send to Kafka                                          │
└───────────────────────────────────────────────────────────┘
         ↓
┌───────────────────────────────────────────────────────────┐
│ Kafka Broker                                               │
│  - Stores binary data (doesn't parse)                     │
└───────────────────────────────────────────────────────────┘
         ↓
┌───────────────────────────────────────────────────────────┐
│ Consumer                                                   │
│   ↓                                                        │
│ 1. Read schema ID from message                            │
│   ↓                                                        │
│ 2. Fetch schema from Registry (cached)                    │
│   ↓                                                        │
│ 3. Deserialize data using schema                          │
│   ↓                                                        │
│ 4. Return Java object                                     │
└───────────────────────────────────────────────────────────┘
```

### Schema Compatibility Modes

**Backward Compatibility** (Default):
- New schema can read data written with old schema
- **Example**: Add optional field with default value
- **Use Case**: Upgrade consumers before producers

**Forward Compatibility**:
- Old schema can read data written with new schema
- **Example**: Remove optional field
- **Use Case**: Upgrade producers before consumers

**Full Compatibility**:
- Both backward and forward compatible
- **Example**: Add optional field with default, never remove fields
- **Use Case**: Upgrade in any order

### Maven Plugin Configuration

```xml
<plugin>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro-maven-plugin</artifactId>
    <version>1.11.3</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>schema</goal>
            </goals>
            <configuration>
                <sourceDirectory>${project.basedir}/src/main/resources/avro/</sourceDirectory>
                <outputDirectory>${project.basedir}/target/generated-sources/avro/</outputDirectory>
                <stringType>String</stringType>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**What It Does**:
- Reads `*.avsc` schema files
- Generates Java classes (POJOs)
- Includes builders, getters, setters
- Happens during `mvn compile`

**Generated Class**:
```
kafka/kafka-model/target/generated-sources/avro/
  └── com/learning/kafka/avro/model/
      └── SocialEventAvroModel.java
```

---

## 4. Implementing Kafka Producers

### Generic Avro Producer

**File**: `kafka/kafka-producer/src/main/java/.../AvroKafkaProducer.java`

```java
@Service
@Slf4j
public class AvroKafkaProducer<K, V> implements KafkaProducer<K, V> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    public AvroKafkaProducer(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, K key, V message) {
        log.info("Sending message='{}' to topic='{}'", message, topicName);

        try {
            // Send asynchronously
            ListenableFuture<SendResult<K, V>> future =
                kafkaTemplate.send(topicName, key, message);

            // Add callback to handle result
            future.addCallback(
                new ListenableFutureCallback<>() {
                    @Override
                    public void onSuccess(SendResult<K, V> result) {
                        RecordMetadata metadata = result.getRecordMetadata();
                        log.debug("Sent message='{}' with key='{}' to " +
                                "topic='{}', partition={}, offset={}",
                            message, key,
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset());
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Failed to send message='{}' to topic='{}'",
                            message, topicName, ex);
                        // Could send to DLQ, retry, or alert
                    }
                }
            );

        } catch (Exception e) {
            log.error("Error sending message to Kafka", e);
        }
    }
}
```

### Key Points

**1. Asynchronous Sending**:
- `kafkaTemplate.send()` returns immediately
- `ListenableFuture` allows callback handling
- Don't block the producer thread

**2. Callback Handling**:
- `onSuccess`: Message acknowledged by Kafka
- `onFailure`: Retry, log, or send to DLQ

**3. Metadata in Success Callback**:
```java
RecordMetadata metadata = result.getRecordMetadata();
- metadata.topic()      // Topic name
- metadata.partition()  // Partition number (0, 1, 2)
- metadata.offset()     // Offset in partition (sequential ID)
- metadata.timestamp()  // Timestamp assigned by broker
```

---

## 5. Event Generation Logic

### Enhanced Mock Stream Runner

**File**: `event-stream-service/src/main/java/.../EnhancedMockStreamRunner.java`

```java
@Component
@Slf4j
public class EnhancedMockStreamRunner implements CommandLineRunner {

    private final EventStreamConfigData eventStreamConfigData;
    private final KafkaProducer<Long, SocialEventAvroModel> kafkaProducer;

    private static final String[] TECH_KEYWORDS = {
        "Java", "Spring Boot", "Kafka", "Microservices",
        "Docker", "Kubernetes", "REST API", "Database"
    };

    private final Random random = new Random();
    private final AtomicLong messageCounter = new AtomicLong(0);
    private ScheduledExecutorService executor;

    @Override
    public void run(String... args) {
        log.info("Starting event stream generation...");

        executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(
            this::generateAndSendEvent,
            0,                                          // Initial delay
            eventStreamConfigData.getSleepMs(),         // Period (1000ms)
            TimeUnit.MILLISECONDS
        );

        // Schedule statistics logging every 30 seconds
        executor.scheduleAtFixedRate(
            this::logStatistics,
            30, 30,
            TimeUnit.SECONDS
        );
    }

    private void generateAndSendEvent() {
        try {
            SocialEventAvroModel event = createEvent();
            kafkaProducer.send(
                eventStreamConfigData.getTopicName(),
                event.getUserId(),
                event
            );
            messageCounter.incrementAndGet();

        } catch (Exception e) {
            log.error("Error generating event", e);
        }
    }

    private SocialEventAvroModel createEvent() {
        long userId = random.nextInt(100) + 1;  // Users 1-100
        long eventId = System.currentTimeMillis();
        String text = generateRealisticText();
        long createdAt = System.currentTimeMillis();

        return SocialEventAvroModel.newBuilder()
            .setUserId(userId)
            .setId(eventId)
            .setText(text)
            .setCreatedAt(createdAt)
            .build();
    }

    private String generateRealisticText() {
        int category = random.nextInt(4);

        switch (category) {
            case 0: // Tech discussion
                return String.format("Exploring %s and %s integration today!",
                    randomKeyword(), randomKeyword());

            case 1: // Tutorial
                return String.format("Just published a tutorial on %s. " +
                    "Check it out!", randomKeyword());

            case 2: // Question
                return String.format("Does anyone have experience with %s " +
                    "in production?", randomKeyword());

            case 3: // Announcement
                return String.format("Our team migrated to %s and seeing " +
                    "great results!", randomKeyword());

            default:
                return "Loving the microservices journey!";
        }
    }

    private String randomKeyword() {
        return TECH_KEYWORDS[random.nextInt(TECH_KEYWORDS.length)];
    }

    private void logStatistics() {
        long count = messageCounter.get();
        log.info("=== Event Stream Statistics ===");
        log.info("Total events generated: {}", count);
        log.info("Rate: {} events/min", count / ((System.currentTimeMillis() / 60000.0)));
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down event stream generation...");
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}
```

### Key Implementation Details

**1. CommandLineRunner**:
```java
@Component
public class EnhancedMockStreamRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Executes after Spring Boot startup
    }
}
```

**2. Scheduled Execution**:
```java
ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

executor.scheduleAtFixedRate(
    this::generateAndSendEvent,
    0,          // Initial delay
    1000,       // Period (1 second)
    TimeUnit.MILLISECONDS
);
```

**3. Builder Pattern**:
```java
SocialEventAvroModel event = SocialEventAvroModel.newBuilder()
    .setUserId(42L)
    .setId(1234567890L)
    .setText("Hello Kafka")
    .setCreatedAt(System.currentTimeMillis())
    .build();
```

**4. Graceful Shutdown**:
```java
@PreDestroy
public void shutdown() {
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
}
```

---

## 6. Producer Configuration

### Configuration File

**File**: `event-stream-service/src/main/resources/application.yml`

```yaml
kafka-config:
  bootstrap-servers:
    - localhost:19092
    - localhost:29092
    - localhost:39092
  schema-registry-url: http://localhost:8081

kafka-producer-config:
  key-serializer-class: org.apache.kafka.common.serialization.LongSerializer
  value-serializer-class: io.confluent.kafka.serializers.KafkaAvroSerializer
  compression-type: snappy
  acks: all
  batch-size: 16384
  linger-ms: 10
  request-timeout-ms: 60000
  retry-count: 3
```

### Configuration Explained

**1. Bootstrap Servers**:
```yaml
bootstrap-servers:
  - localhost:19092
  - localhost:29092
  - localhost:39092
```
- List of Kafka brokers
- Producer connects to any available broker
- Broker returns full cluster metadata

**2. Serializers**:
```yaml
key-serializer-class: org.apache.kafka.common.serialization.LongSerializer
value-serializer-class: io.confluent.kafka.serializers.KafkaAvroSerializer
```
- **Key**: Long (userId) serialized to 8 bytes
- **Value**: Avro object serialized to binary

**3. Compression**:
```yaml
compression-type: snappy
```
- **snappy**: Fast, moderate compression (~2x)
- Alternatives: `gzip` (slower, better compression), `lz4`, `zstd`

**4. Acknowledgments**:
```yaml
acks: all
```
| Value | Meaning | Durability | Latency |
|-------|---------|------------|---------|
| `0` | Fire and forget | Low | Lowest |
| `1` | Leader acknowledges | Medium | Low |
| `all` | All in-sync replicas | High | Higher |

**5. Batching**:
```yaml
batch-size: 16384  # 16 KB
linger-ms: 10      # Wait up to 10ms
```
- Producer waits up to 10ms to batch more events
- Sends batch when 16KB reached or 10ms elapsed
- **Trade-off**: Latency vs throughput

**6. Retries**:
```yaml
retry-count: 3
request-timeout-ms: 60000
```
- Retry failed sends up to 3 times
- Timeout after 60 seconds

### Spring Configuration Class

```java
@Configuration
public class KafkaProducerConfig {

    private final KafkaProducerConfigData kafkaProducerConfigData;

    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            kafkaConfigData.getBootstrapServers());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            kafkaProducerConfigData.getKeySerializerClass());

        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            kafkaProducerConfigData.getValueSerializerClass());

        props.put(ProducerConfig.ACKS_CONFIG,
            kafkaProducerConfigData.getAcks());

        props.put(ProducerConfig.BATCH_SIZE_CONFIG,
            kafkaProducerConfigData.getBatchSize());

        props.put(ProducerConfig.LINGER_MS_CONFIG,
            kafkaProducerConfigData.getLingerMs());

        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,
            kafkaProducerConfigData.getCompressionType());

        props.put(ProducerConfig.RETRIES_CONFIG,
            kafkaProducerConfigData.getRetryCount());

        // Schema Registry URL
        props.put("schema.registry.url",
            kafkaConfigData.getSchemaRegistryUrl());

        return props;
    }

    @Bean
    public ProducerFactory<Long, SocialEventAvroModel> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<Long, SocialEventAvroModel> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

---

## 7. Error Handling

### Producer Error Types

**1. Retriable Errors**:
- Network timeout
- Leader not available
- Not enough replicas

**2. Non-Retriable Errors**:
- Invalid message size (too large)
- Serialization error
- Authorization failure

### Callback Error Handling

```java
future.addCallback(
    new ListenableFutureCallback<>() {
        @Override
        public void onFailure(Throwable ex) {
            if (ex instanceof RetriableException) {
                // Kafka will retry automatically
                log.warn("Retriable error, Kafka will retry: {}", ex.getMessage());
            } else {
                // Non-retriable error
                log.error("Non-retriable error, sending to DLQ", ex);
                sendToDeadLetterQueue(message, ex);
            }
        }
    }
);
```

### Dead Letter Queue (DLQ)

```java
private void sendToDeadLetterQueue(SocialEventAvroModel event, Throwable error) {
    String dlqTopic = "social-events.DLQ";

    // Add error metadata
    Map<String, String> headers = new HashMap<>();
    headers.put("error", error.getClass().getName());
    headers.put("error-message", error.getMessage());
    headers.put("timestamp", String.valueOf(System.currentTimeMillis()));

    kafkaTemplate.send(dlqTopic, event.getUserId(), event);
    log.info("Sent failed event to DLQ: {}", dlqTopic);
}
```

---

## 8. Performance Tuning

### Producer Throughput Optimization

**1. Increase Batch Size**:
```yaml
batch-size: 32768  # 32 KB (default: 16 KB)
linger-ms: 20      # Wait longer to accumulate more events
```
**Result**: Higher throughput, slightly higher latency

**2. Compression**:
```yaml
compression-type: lz4  # Faster than snappy
```
**Result**: Reduced network bandwidth, lower latency

**3. Parallel Producers**:
```java
// Use multiple producer threads
ExecutorService executor = Executors.newFixedThreadPool(10);

for (int i = 0; i < 100; i++) {
    executor.submit(() -> kafkaProducer.send(topic, key, event));
}
```

**4. Increase Buffer Memory**:
```yaml
buffer-memory: 67108864  # 64 MB (default: 32 MB)
```
**Result**: More events buffered, better burst handling

### Monitoring Producer Metrics

```java
@Component
public class ProducerMetrics {

    private final MeterRegistry meterRegistry;

    @Autowired
    public ProducerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordSent(String topic) {
        Counter.builder("kafka.producer.sent")
            .tag("topic", topic)
            .register(meterRegistry)
            .increment();
    }

    public void recordFailed(String topic, String error) {
        Counter.builder("kafka.producer.failed")
            .tag("topic", topic)
            .tag("error", error)
            .register(meterRegistry)
            .increment();
    }
}
```

**View Metrics**:
```bash
curl http://localhost:8080/actuator/prometheus | grep kafka_producer
```

---

## 9. Summary

### Key Takeaways

✅ **Apache Avro** provides compact, schema-based serialization
✅ **Schema Registry** enables centralized schema management and evolution
✅ **Maven plugin** generates Java classes from Avro schemas
✅ **KafkaTemplate** simplifies producing messages with Spring Boot
✅ **Asynchronous callbacks** handle success/failure without blocking
✅ **Configuration tuning** balances latency, throughput, and durability
✅ **Error handling** uses retries and dead letter queues

### Producer Flow Recap

```
1. Define Avro Schema (.avsc)
       ↓
2. Maven generates Java class
       ↓
3. Build event object
       ↓
4. KafkaProducer.send(topic, key, event)
       ↓
5. Serializer looks up schema in Registry
       ↓
6. Serialize to binary with schema ID
       ↓
7. Send to Kafka broker
       ↓
8. Callback: onSuccess or onFailure
```

### Best Practices

✅ **Use Schema Registry** for all Avro messages
✅ **Set acks=all** for critical data
✅ **Enable compression** (snappy or lz4)
✅ **Implement callbacks** for error handling
✅ **Monitor metrics** (sent, failed, latency)
✅ **Test schema evolution** before deploying
✅ **Use DLQ** for unrecoverable errors

---

## Next Steps

You've mastered event production! Now learn how to consume those events efficiently.

👉 **[Proceed to Module 4: Event Consumption Patterns](./04-event-consumption.md)**

---

## Hands-On Exercises

### Exercise 1: Add New Field to Schema

**Task**: Add an optional `location` field (string) to the schema.

**Steps**:
1. Edit `social-event.avsc`
2. Add field: `{"name": "location", "type": ["null", "string"], "default": null}`
3. Run `mvn clean compile` to regenerate classes
4. Update producer to set location
5. Verify schema registered in Schema Registry

**Verify**:
```bash
curl http://localhost:8081/subjects/social-events-value/versions
```

### Exercise 2: Implement Custom Partitioner

**Task**: Ensure all events from the same user go to the same partition.

**Implementation**:
```java
public class UserPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                        Object value, byte[] valueBytes,
                        Cluster cluster) {
        int partitionCount = cluster.partitionCountForTopic(topic);
        return Math.abs(key.hashCode()) % partitionCount;
    }
}
```

**Configuration**:
```yaml
partitioner-class: com.example.UserPartitioner
```

### Exercise 3: Measure Producer Latency

**Task**: Measure time from send to acknowledgment.

**Code**:
```java
long startTime = System.nanoTime();

future.addCallback(result -> {
    long latency = System.nanoTime() - startTime;
    log.info("Latency: {} ms", latency / 1_000_000.0);
}, ex -> {});
```

**Analysis**: Run for 1000 events, calculate average, p50, p95, p99.

---

**Module Progress**: 3 of 10 complete
