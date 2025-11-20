# Hands-On Exercises

This directory contains practical exercises to reinforce the concepts learned in each module.

---

## Exercise Structure

Each exercise includes:
- **Objective**: What you'll build or learn
- **Difficulty**: Beginner, Intermediate, or Advanced
- **Duration**: Estimated time to complete
- **Prerequisites**: Required knowledge or modules
- **Instructions**: Step-by-step guidance
- **Hints**: Tips to help you succeed
- **Solution**: Reference implementation (in separate file)

---

## Module-Based Exercises

### Module 1: Foundational Concepts

#### Exercise 1.1: Kafka Basics Quiz
**Difficulty**: Beginner | **Duration**: 15 minutes

Test your understanding of core Kafka concepts.

**Questions**:

1. **Partitions and Parallelism**
   - A topic has 5 partitions. What is the maximum number of consumers in a consumer group that can actively read from this topic?
   - Answer: 5 (one per partition)

2. **Acknowledgments**
   - If `acks=all` and replication factor is 3, how many brokers must acknowledge a write?
   - Answer: All 3 in-sync replicas (ISR)

3. **Offset Management**
   - A consumer crashes before committing offsets. What happens when it restarts?
   - Answer: It reprocesses events from last committed offset (at-least-once delivery)

4. **Partition Assignment**
   - Can events with different keys end up in the same partition?
   - Answer: Yes, if hash(key) % partitions is the same

5. **Auto Offset Reset**
   - What's the difference between `auto-offset-reset: earliest` and `latest`?
   - Answer: earliest starts from beginning, latest only reads new events

#### Exercise 1.2: Design an Event-Driven E-Commerce System
**Difficulty**: Intermediate | **Duration**: 30 minutes

**Scenario**: Design a simple e-commerce system with event-driven architecture.

**Requirements**:
- **Services**: Order Service, Inventory Service, Notification Service, Analytics Service
- **Events**: OrderPlaced, InventoryReserved, OrderShipped, OrderCancelled
- **Topics**: Design topics and their consumers

**Your Task**:
1. Draw architecture diagram
2. List all topics needed
3. Specify which service produces which events
4. Specify which service consumes which events
5. Explain the data flow for placing an order

**Solution**:

```
Topics:
1. order-events (OrderPlaced, OrderCancelled)
2. inventory-events (InventoryReserved, InventoryReleased)
3. shipping-events (OrderShipped, OrderDelivered)

Producers:
- Order Service → order-events
- Inventory Service → inventory-events
- Shipping Service → shipping-events

Consumers:
- Inventory Service ← order-events (reserves inventory)
- Shipping Service ← inventory-events (ships when reserved)
- Notification Service ← all topics (sends emails)
- Analytics Service ← all topics (tracks metrics)

Flow for placing order:
1. User places order → Order Service publishes OrderPlaced
2. Inventory Service consumes OrderPlaced → reserves items → publishes InventoryReserved
3. Shipping Service consumes InventoryReserved → ships order → publishes OrderShipped
4. Notification Service sends emails at each step
5. Analytics Service tracks conversion funnel
```

#### Exercise 1.3: Calculate Throughput
**Difficulty**: Intermediate | **Duration**: 15 minutes

**Given**:
- Topic with 3 partitions
- 3 consumers (one per partition)
- Each consumer processes 1000 events/second

**Questions**:

1. What's the total throughput?
   - Answer: 3000 events/second (1000 × 3)

2. If you add 2 more partitions (total 5) but keep 3 consumers, what changes?
   - Answer: Throughput stays ~3000/s. Some consumers handle 2 partitions, but processing is still limited by consumer capacity.

3. If you add 2 more consumers (total 5) but keep 3 partitions, what changes?
   - Answer: Throughput stays 3000/s. 2 consumers are idle (max consumers = partitions).

4. What's the optimal configuration for 10,000 events/second if each consumer can handle 1000/s?
   - Answer: 10 partitions, 10 consumers

---

### Module 2: Project Architecture

#### Exercise 2.1: Service Port Mapping
**Difficulty**: Beginner | **Duration**: 10 minutes

**Task**: Create a reference table of all services and ports.

| Service | Port | Purpose | Health Check |
|---------|------|---------|--------------|
| Event Stream Service | 8080 | Event generation | http://localhost:8080/actuator/health |
| Kafka Consumer Service | 8081 | Batch consumption | http://localhost:8081/actuator/health |
| ... | ... | ... | ... |

Complete the table for all 6 services plus infrastructure.

#### Exercise 2.2: Trace a Single Event
**Difficulty**: Intermediate | **Duration**: 30 minutes

**Task**: Trace the journey of a single event through the entire system.

**Steps**:
1. Start all services
2. Enable DEBUG logging for all services
3. Watch logs in all terminals
4. Identify a single event by its ID
5. Document each step it goes through:
   - Generation (Event Stream Service)
   - Kafka partition assignment
   - Consumption (3 consumers)
   - Elasticsearch indexing
   - API query
   - UI display

**Output**: Timeline document showing timestamps at each step.

#### Exercise 2.3: Add a New Consumer Service
**Difficulty**: Advanced | **Duration**: 90 minutes

**Task**: Create a new consumer service that tracks user activity.

**Requirements**:
1. Create Spring Boot project: `user-activity-service`
2. Consumer group: `user-activity-group`
3. Track events per user (ConcurrentHashMap)
4. Log top 10 most active users every 30 seconds
5. Port: 8085
6. Health check endpoint

**Starter Code**:
```java
@Service
public class UserActivityConsumer {

    private final Map<Long, AtomicLong> userCounts = new ConcurrentHashMap<>();

    @KafkaListener(topics = "social-events", groupId = "user-activity-group")
    public void consume(SocialEventAvroModel event) {
        userCounts.computeIfAbsent(event.getUserId(), k -> new AtomicLong(0))
                  .incrementAndGet();
    }

    @Scheduled(fixedRate = 30000)
    public void logTopUsers() {
        List<Map.Entry<Long, AtomicLong>> top10 = userCounts.entrySet()
            .stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
            .limit(10)
            .collect(Collectors.toList());

        log.info("=== Top 10 Active Users ===");
        top10.forEach(entry ->
            log.info("User {}: {} events", entry.getKey(), entry.getValue().get())
        );
    }
}
```

---

### Module 3: Event Production

#### Exercise 3.1: Add New Field to Avro Schema
**Difficulty**: Beginner | **Duration**: 30 minutes

**Task**: Add an optional `location` field to the SocialEvent schema.

**Steps**:
1. Edit `kafka/kafka-model/src/main/resources/avro/social-event.avsc`
2. Add field:
   ```json
   {
     "name": "location",
     "type": ["null", "string"],
     "default": null,
     "doc": "Location where the event was created (optional)"
   }
   ```
3. Run `mvn clean compile` in `kafka-model` module
4. Verify generated class has `getLocation()` and `setLocation()`
5. Update producer to set location (e.g., random city)
6. Run Event Stream Service
7. Consume and verify field is present

**Verification**:
```bash
# Check Schema Registry
curl http://localhost:8081/subjects/social-events-value/versions

# Should show new version with location field
```

#### Exercise 3.2: Implement Custom Partitioner
**Difficulty**: Intermediate | **Duration**: 45 minutes

**Task**: Ensure all events from the same user go to the same partition.

**Implementation**:

```java
public class UserIdPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                        Object value, byte[] valueBytes,
                        Cluster cluster) {
        int partitionCount = cluster.partitionCountForTopic(topic);

        // Hash userId to determine partition
        Long userId = (Long) key;
        return Math.abs(userId.hashCode()) % partitionCount;
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}
```

**Configuration**:
```yaml
kafka:
  producer:
    partitioner-class: com.learning.kafka.partitioner.UserIdPartitioner
```

**Verification**:
1. Send 100 events from user 42
2. Consume from each partition individually
3. Verify all user 42 events are in the same partition

#### Exercise 3.3: Measure Producer Latency
**Difficulty**: Intermediate | **Duration**: 45 minutes

**Task**: Measure end-to-end latency from send to acknowledgment.

**Implementation**:

```java
@Service
public class LatencyMeasuringProducer {

    private final KafkaTemplate<Long, SocialEventAvroModel> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    public void sendWithLatencyTracking(String topic, Long key, SocialEventAvroModel event) {
        long startTime = System.nanoTime();

        ListenableFuture<SendResult<Long, SocialEventAvroModel>> future =
            kafkaTemplate.send(topic, key, event);

        future.addCallback(
            result -> {
                long latency = System.nanoTime() - startTime;
                double latencyMs = latency / 1_000_000.0;

                meterRegistry.timer("kafka.producer.latency", "topic", topic)
                             .record(latency, TimeUnit.NANOSECONDS);

                log.debug("Send latency: {} ms", latencyMs);
            },
            ex -> {
                long latency = System.nanoTime() - startTime;
                log.error("Send failed after {} ms", latency / 1_000_000.0);
            }
        );
    }
}
```

**Analysis**:
1. Send 1000 events
2. Calculate statistics:
   - Average latency
   - p50 (median)
   - p95
   - p99
3. Experiment with different configurations:
   - `linger.ms`: 0, 10, 50, 100
   - `batch.size`: 16KB, 32KB, 64KB
   - `acks`: 1, all

**Expected Results**:
```
Configuration: acks=all, linger.ms=10, batch.size=16KB
Average: 15ms
p50: 12ms
p95: 28ms
p99: 45ms
```

---

### Module 4: Event Consumption

#### Exercise 4.1: Implement Idempotent Consumer
**Difficulty**: Intermediate | **Duration**: 60 minutes

**Task**: Build a consumer that processes each event exactly once, even with redeliveries.

**Approach**: Store processed event IDs in a Set.

```java
@Service
public class IdempotentConsumer {

    private final Set<Long> processedEventIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "social-events", groupId = "idempotent-group")
    public void consume(SocialEventAvroModel event, Acknowledgment ack) {
        Long eventId = event.getId();

        // Check if already processed
        if (processedEventIds.contains(eventId)) {
            log.warn("Duplicate event detected: {}", eventId);
            ack.acknowledge();  // Commit anyway
            return;
        }

        try {
            // Process event
            processEvent(event);

            // Mark as processed
            processedEventIds.add(eventId);

            // Commit offset
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process event: {}", eventId, e);
            // Don't acknowledge; event will be redelivered
        }
    }

    private void processEvent(SocialEventAvroModel event) {
        // Business logic here
        log.info("Processing event: {}", event.getId());
    }
}
```

**Testing**:
1. Process 100 events
2. Simulate crash (kill service before commit)
3. Restart service
4. Verify no events are processed twice

#### Exercise 4.2: Implement Dead Letter Queue
**Difficulty**: Intermediate | **Duration**: 60 minutes

**Task**: Send failed events to a DLQ topic after 3 retries.

```java
@Service
public class DLQConsumer {

    private final Map<Long, Integer> retryCount = new ConcurrentHashMap<>();
    private final KafkaTemplate<Long, SocialEventAvroModel> kafkaTemplate;

    @KafkaListener(topics = "social-events", groupId = "dlq-group")
    public void consume(SocialEventAvroModel event, Acknowledgment ack) {
        Long eventId = event.getId();

        try {
            processEvent(event);
            retryCount.remove(eventId);
            ack.acknowledge();

        } catch (Exception e) {
            int attempts = retryCount.getOrDefault(eventId, 0) + 1;
            retryCount.put(eventId, attempts);

            if (attempts >= 3) {
                log.error("Event {} failed after {} attempts, sending to DLQ", eventId, attempts);
                sendToDLQ(event, e);
                retryCount.remove(eventId);
                ack.acknowledge();  // Don't reprocess
            } else {
                log.warn("Event {} failed, attempt {}/3", eventId, attempts);
                // Don't acknowledge; will be redelivered
            }
        }
    }

    private void sendToDLQ(SocialEventAvroModel event, Exception error) {
        kafkaTemplate.send("social-events.DLQ", event.getUserId(), event);
    }
}
```

---

### Module 5: Stream Processing

#### Exercise 5.1: Implement Sentiment Analysis
**Difficulty**: Advanced | **Duration**: 90 minutes

**Task**: Add sentiment field (POSITIVE, NEGATIVE, NEUTRAL) to events.

**Step 1**: Update Avro Schema
```json
{
  "name": "sentiment",
  "type": {
    "type": "enum",
    "name": "Sentiment",
    "symbols": ["POSITIVE", "NEGATIVE", "NEUTRAL"]
  },
  "default": "NEUTRAL"
}
```

**Step 2**: Sentiment Analysis Function
```java
public Sentiment analyzeSentiment(String text) {
    String lowerText = text.toLowerCase();

    String[] positiveWords = {"great", "awesome", "love", "excellent", "amazing"};
    String[] negativeWords = {"bad", "hate", "terrible", "awful", "horrible"};

    long positiveCount = Arrays.stream(positiveWords)
        .filter(lowerText::contains)
        .count();

    long negativeCount = Arrays.stream(negativeWords)
        .filter(lowerText::contains)
        .count();

    if (positiveCount > negativeCount) {
        return Sentiment.POSITIVE;
    } else if (negativeCount > positiveCount) {
        return Sentiment.NEGATIVE;
    } else {
        return Sentiment.NEUTRAL;
    }
}
```

**Step 3**: Add to Stream Topology
```java
KStream<Long, SocialEventAvroModel> enriched =
    stream.mapValues(event -> {
        Sentiment sentiment = analyzeSentiment(event.getText().toString());
        event.setSentiment(sentiment);
        return event;
    });

// Count by sentiment per 5-min window
KTable<Windowed<Sentiment>, Long> sentimentCounts =
    enriched
        .groupBy((key, value) -> value.getSentiment())
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
        .count();

sentimentCounts.toStream()
    .to("sentiment-analytics");
```

---

### Challenge Projects

#### Challenge 1: Real-Time Trending Topics
**Difficulty**: Advanced | **Duration**: 3-4 hours

**Objective**: Build a trending topics detector using Kafka Streams.

**Requirements**:
1. Extract hashtags from event text (e.g., #kafka, #microservices)
2. Count hashtags in 15-minute sliding windows (advance every 5 minutes)
3. Identify top 10 hashtags per window
4. Only consider hashtags that appear at least 5 times
5. Create REST API to query current trending topics
6. Build simple UI to display trends

**Bonus**: Add trend velocity (trending up/down).

#### Challenge 2: User Session Analytics
**Difficulty**: Advanced | **Duration**: 3-4 hours

**Objective**: Analyze user sessions using session windows.

**Requirements**:
1. Define session as events within 5-minute inactivity gap
2. Calculate metrics per session:
   - Duration
   - Event count
   - Average time between events
3. Store session summaries in Elasticsearch
4. Create dashboard showing:
   - Average session duration
   - Average events per session
   - Session distribution histogram

#### Challenge 3: Anomaly Detection
**Difficulty**: Expert | **Duration**: 5-6 hours

**Objective**: Detect abnormal user behavior in real-time.

**Requirements**:
1. Calculate baseline: average events per user per hour (7-day window)
2. Detect anomalies: user activity > 3× standard deviation
3. Send alerts to `anomaly-alerts` topic
4. Include context:
   - User ID
   - Expected rate
   - Actual rate
   - Severity (warning, critical)
5. Create monitoring dashboard

---

## Additional Resources

### Learning Paths

**Kafka Fundamentals Path**:
1. Module 1 Exercises
2. Module 3 Exercise 3.1 (Schema Evolution)
3. Module 4 Exercise 4.1 (Idempotent Consumer)
4. Module 4 Exercise 4.2 (DLQ)

**Stream Processing Path**:
1. Module 5 Exercise 5.1 (Sentiment Analysis)
2. Challenge 1 (Trending Topics)
3. Challenge 2 (User Sessions)
4. Challenge 3 (Anomaly Detection)

**Full-Stack Path**:
1. Module 2 Exercise 2.3 (New Service)
2. Module 7 Exercises (REST API)
3. Module 9 Exercises (React UI)
4. Challenge 1 or 2 (End-to-end feature)

### Practice Datasets

**Generate Realistic Test Data**:
```bash
# Generate 10,000 events
curl -X POST http://localhost:8080/api/generate?count=10000

# Generate events with specific pattern
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{"pattern": "burst", "users": [1,2,3], "duration_minutes": 10}'
```

### Community Challenges

Join the community and tackle these challenges:

1. **Kafka Connect Challenge**: Integrate with external database
2. **ksqlDB Challenge**: Implement stream processing with SQL
3. **Multi-Region Challenge**: Deploy across multiple data centers
4. **Performance Challenge**: Achieve 1M events/second throughput

---

**Happy Learning!** 🚀
