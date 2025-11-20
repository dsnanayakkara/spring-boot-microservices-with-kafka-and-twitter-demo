# Module 1: Foundational Concepts

**Duration**: 60 minutes
**Difficulty**: Beginner
**Prerequisites**: Basic Java knowledge

---

## Learning Objectives

By the end of this module, you will understand:

✅ What Event-Driven Architecture is and why it matters
✅ Core Apache Kafka concepts (topics, partitions, brokers)
✅ How producers and consumers work
✅ Consumer groups and parallel processing
✅ Offset management and delivery semantics
✅ When to use Kafka vs traditional messaging systems

---

## Table of Contents

1. [Event-Driven Architecture](#1-event-driven-architecture)
2. [Apache Kafka Fundamentals](#2-apache-kafka-fundamentals)
3. [Topics and Partitions](#3-topics-and-partitions)
4. [Producers](#4-producers)
5. [Consumers](#5-consumers)
6. [Consumer Groups](#6-consumer-groups)
7. [Offset Management](#7-offset-management)
8. [Delivery Semantics](#8-delivery-semantics)
9. [When to Use Kafka](#9-when-to-use-kafka)
10. [Summary](#10-summary)

---

## 1. Event-Driven Architecture

### What is an Event?

An **event** is a record of something that happened in your system at a specific point in time.

**Examples**:
- User registered (userId: 123, email: user@example.com, timestamp: 2024-11-20T10:30:00Z)
- Order placed (orderId: 456, total: $99.99, timestamp: 2024-11-20T10:31:15Z)
- Temperature reading (sensorId: sensor-42, value: 72.5°F, timestamp: 2024-11-20T10:32:00Z)

**Key Characteristics**:
- **Immutable**: Events describe what happened; they cannot be changed
- **Time-stamped**: Events always include when they occurred
- **Self-contained**: Events contain all relevant data

### Traditional Request-Response vs Event-Driven

#### Traditional Architecture (Synchronous)

```
┌─────────────┐                    ┌─────────────┐
│  Service A  │──── HTTP POST ────▶│  Service B  │
│             │◀─── Response ──────│             │
└─────────────┘                    └─────────────┘
     ↓ waits
```

**Characteristics**:
- **Tight coupling**: Service A knows about Service B
- **Synchronous**: Service A blocks waiting for response
- **Brittle**: If Service B is down, Service A fails
- **Limited scalability**: One request = one response

#### Event-Driven Architecture (Asynchronous)

```
                         ┌─────────────────┐
                         │   Event Bus     │
                         │    (Kafka)      │
                         └─────────────────┘
                                  ▲
                                  │
┌─────────────┐                  │                  ┌─────────────┐
│  Service A  │──── Publish ─────┤                  │  Service B  │
│ (Producer)  │                  │                  │ (Consumer)  │
└─────────────┘                  │                  └─────────────┘
                                  │
                                  │                  ┌─────────────┐
                                  ├─── Subscribe ───▶│  Service C  │
                                  │                  │ (Consumer)  │
                                  │                  └─────────────┘
                                  │
                                  │                  ┌─────────────┐
                                  └─── Subscribe ───▶│  Service D  │
                                                     │ (Consumer)  │
                                                     └─────────────┘
```

**Characteristics**:
- **Loose coupling**: Producers don't know about consumers
- **Asynchronous**: Producers don't wait for consumers
- **Resilient**: Services can fail and recover independently
- **Scalable**: Multiple consumers can process events in parallel
- **Extensible**: Add new consumers without changing producers

### Benefits of Event-Driven Architecture

| Benefit | Description | Example |
|---------|-------------|---------|
| **Decoupling** | Services don't need to know about each other | Add email service without changing user service |
| **Scalability** | Scale consumers independently based on load | Add more instances during peak hours |
| **Resilience** | Failures are isolated; events are persisted | Service restarts and processes missed events |
| **Auditability** | Event log serves as audit trail | Replay events to understand what happened |
| **Flexibility** | Easy to add new consumers | Add analytics service without code changes |

---

## 2. Apache Kafka Fundamentals

### What is Apache Kafka?

**Apache Kafka** is a distributed event streaming platform that lets you:
- **Publish** events (write)
- **Subscribe** to events (read)
- **Store** events durably (persistence)
- **Process** events as they occur (stream processing)

Think of Kafka as a **high-performance, distributed commit log**.

### Core Kafka Concepts

```
┌───────────────────────────────────────────────────────────────────┐
│                        KAFKA CLUSTER                               │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │  Broker 1    │  │  Broker 2    │  │  Broker 3    │           │
│  │              │  │              │  │              │           │
│  │  Topic: A    │  │  Topic: A    │  │  Topic: A    │           │
│  │  Partition 0 │  │  Partition 1 │  │  Partition 2 │           │
│  │              │  │              │  │              │           │
│  │  Topic: B    │  │  Topic: B    │  │  Topic: B    │           │
│  │  Partition 0 │  │  Partition 1 │  │  Partition 2 │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
│                                                                    │
└───────────────────────────────────────────────────────────────────┘
           ▲                                        │
           │                                        ▼
     ┌──────────┐                            ┌──────────┐
     │ Producer │                            │ Consumer │
     └──────────┘                            └──────────┘
```

### Key Components

#### 1. **Broker**
- A Kafka server that stores and serves data
- Multiple brokers form a cluster
- Each broker can handle thousands of reads/writes per second

#### 2. **Topic**
- A category or feed name to which events are published
- Like a database table or a folder
- Topics are split into partitions for scalability

#### 3. **Partition**
- An ordered, immutable sequence of events
- Each partition is replicated across brokers for fault tolerance
- Enables parallel processing

#### 4. **Producer**
- An application that publishes events to topics
- Decides which partition to send each event to

#### 5. **Consumer**
- An application that subscribes to topics and processes events
- Reads events in order within each partition

#### 6. **ZooKeeper** (or KRaft in Kafka 3.x+)
- Manages cluster metadata and coordination
- Tracks which brokers are alive
- Elects partition leaders

---

## 3. Topics and Partitions

### Topics: Organizing Events

A **topic** is a logical channel for events of the same type.

**Examples**:
- `user-registrations`: All user registration events
- `order-placed`: All order placement events
- `sensor-readings`: All IoT sensor data

**Topic Properties**:
- **Name**: Unique identifier (e.g., "social-events")
- **Partitions**: Number of parallel channels (e.g., 3)
- **Replication Factor**: Number of copies for fault tolerance (e.g., 3)
- **Retention**: How long to keep events (e.g., 7 days)

### Partitions: Enabling Scalability

**Why Partitions?**

A single partition can only be read by one consumer thread at a time. Partitions enable:
1. **Parallelism**: Multiple consumers process different partitions simultaneously
2. **Scalability**: Add more partitions to handle more throughput
3. **Ordering**: Events within a partition are strictly ordered

### Partition Structure

```
Topic: social-events (3 partitions)

Partition 0:  [Event 0] → [Event 3] → [Event 6] → [Event 9]  → ...
Partition 1:  [Event 1] → [Event 4] → [Event 7] → [Event 10] → ...
Partition 2:  [Event 2] → [Event 5] → [Event 8] → [Event 11] → ...

              Oldest                                       Newest
              (Offset 0)                                  (Latest)
```

**Key Points**:
- Each partition is an ordered, append-only log
- Events are assigned an **offset** (sequential ID within partition)
- Offsets are unique within a partition, not globally
- New events are always appended to the end

### Partition Assignment

When a producer sends an event, how does Kafka decide which partition?

**Strategy 1: Round-Robin** (No key specified)
```
Event 1 → Partition 0
Event 2 → Partition 1
Event 3 → Partition 2
Event 4 → Partition 0 (wraps around)
...
```

**Strategy 2: Key-Based Hashing** (Key specified)
```
Event(key="user-123") → hash("user-123") % 3 = Partition 1
Event(key="user-456") → hash("user-456") % 3 = Partition 2
Event(key="user-123") → hash("user-123") % 3 = Partition 1 (same!)
```

**Why Keys Matter**:
- Events with the same key always go to the same partition
- Guarantees ordering for related events
- Example: All events for user-123 are in order

### Replication and Fault Tolerance

```
Topic: social-events
Partitions: 3
Replication Factor: 3

Partition 0:
  Leader:    Broker 1 (handles reads/writes)
  Replicas:  Broker 2, Broker 3 (backup copies)

Partition 1:
  Leader:    Broker 2
  Replicas:  Broker 1, Broker 3

Partition 2:
  Leader:    Broker 3
  Replicas:  Broker 1, Broker 2
```

**Replication Benefits**:
- **High Availability**: If Broker 1 fails, Broker 2 or 3 becomes leader
- **Data Durability**: Multiple copies prevent data loss
- **Disaster Recovery**: Cluster survives broker failures

**In-Sync Replicas (ISR)**:
- Replicas that are caught up with the leader
- Leader won't acknowledge writes until all ISRs confirm
- Ensures durability

---

## 4. Producers

### Producer Role

A **producer** is an application that:
1. Creates events
2. Serializes them (converts to bytes)
3. Sends them to Kafka topics
4. Receives acknowledgments

### Producer Code Example

```java
// Simple Kafka Producer Configuration
public class SimpleProducer {

    private final KafkaTemplate<Long, SocialEventAvroModel> kafkaTemplate;

    public void sendEvent(SocialEventAvroModel event) {
        // Send event to topic
        ListenableFuture<SendResult<Long, SocialEventAvroModel>> future =
            kafkaTemplate.send("social-events", event.getUserId(), event);

        // Add callback to handle result
        future.addCallback(
            result -> {
                // Success
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.println("Event sent to partition: " +
                    metadata.partition() +
                    ", offset: " + metadata.offset());
            },
            ex -> {
                // Failure
                System.err.println("Failed to send event: " + ex.getMessage());
            }
        );
    }
}
```

### Producer Configuration

Key configuration properties:

```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:19092,localhost:29092,localhost:39092
      key-serializer: org.apache.kafka.common.serialization.LongSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      acks: all                    # Wait for all replicas to acknowledge
      retries: 3                   # Retry failed sends 3 times
      batch-size: 16384            # Batch size in bytes
      linger-ms: 10                # Wait up to 10ms to batch more events
      compression-type: snappy     # Compress batches with Snappy
```

### Producer Acknowledgments (acks)

Determines when Kafka considers a write successful:

| Setting | Behavior | Durability | Performance |
|---------|----------|------------|-------------|
| `acks=0` | Fire and forget (no wait) | Low | High |
| `acks=1` | Wait for leader only | Medium | Medium |
| `acks=all` | Wait for all in-sync replicas | High | Lower |

**Recommendation**: Use `acks=all` for critical data.

### Producer Batching

Producers batch multiple events for efficiency:

```
Individual Sends (Inefficient):
Event 1 → Network → Kafka
Event 2 → Network → Kafka
Event 3 → Network → Kafka
...

Batched Sends (Efficient):
Events [1, 2, 3, 4, 5] → Single Network Request → Kafka
```

**Configuration**:
- `batch-size`: Maximum bytes per batch (default: 16KB)
- `linger.ms`: Max time to wait for more events (default: 0ms)

**Example**: With `linger.ms=10`, producer waits up to 10ms to collect more events before sending.

---

## 5. Consumers

### Consumer Role

A **consumer** is an application that:
1. Subscribes to one or more topics
2. Reads events from partitions
3. Deserializes events (bytes to objects)
4. Processes events
5. Commits offsets (marks progress)

### Consumer Code Example

```java
// Simple Kafka Consumer
@Service
public class SimpleConsumer {

    @KafkaListener(
        topics = "social-events",
        groupId = "my-consumer-group"
    )
    public void consume(SocialEventAvroModel event) {
        System.out.println("Received event: " + event);

        // Process the event
        processEvent(event);

        // Offset is automatically committed after method returns
    }

    private void processEvent(SocialEventAvroModel event) {
        // Business logic here
        System.out.println("User " + event.getUserId() +
            " posted: " + event.getText());
    }
}
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
      auto-offset-reset: earliest     # Start from beginning if no offset
      enable-auto-commit: false       # Manual commit control
      max-poll-records: 500           # Fetch up to 500 records per poll
```

### Batch Consumption

Process multiple events at once for efficiency:

```java
@KafkaListener(
    topics = "social-events",
    groupId = "batch-consumer-group",
    batch = "true"
)
public void consumeBatch(List<SocialEventAvroModel> events) {
    System.out.println("Processing batch of " + events.size() + " events");

    // Process all events in batch
    events.forEach(this::processEvent);

    // Commit offset for entire batch
}
```

**Benefits of Batching**:
- Reduced network overhead
- Better throughput (events/second)
- More efficient database writes (bulk inserts)

---

## 6. Consumer Groups

### What is a Consumer Group?

A **consumer group** is a set of consumers working together to consume a topic.

**Key Principle**: Each partition is consumed by exactly one consumer in the group.

### Consumer Group Example

```
Topic: social-events (3 partitions)

Consumer Group: "analytics-group"
  Consumer 1 → reads from Partition 0
  Consumer 2 → reads from Partition 1
  Consumer 3 → reads from Partition 2

All consumers in the group share the workload!
```

### Partition Assignment

Kafka automatically assigns partitions to consumers:

**Scenario 1: Consumers = Partitions (Perfect Balance)**

```
Partitions: [P0, P1, P2]
Consumers:  [C1, C2, C3]

Assignment:
  C1 → P0
  C2 → P1
  C3 → P2

Each consumer gets one partition.
```

**Scenario 2: More Partitions than Consumers**

```
Partitions: [P0, P1, P2, P3, P4, P5]
Consumers:  [C1, C2, C3]

Assignment:
  C1 → P0, P1
  C2 → P2, P3
  C3 → P4, P5

Each consumer gets two partitions.
```

**Scenario 3: More Consumers than Partitions**

```
Partitions: [P0, P1, P2]
Consumers:  [C1, C2, C3, C4, C5]

Assignment:
  C1 → P0
  C2 → P1
  C3 → P2
  C4 → (idle)
  C5 → (idle)

Extra consumers are idle (wasted resources).
```

**Key Insight**: Maximum parallelism = number of partitions. Always ensure partitions ≥ consumers.

### Consumer Rebalancing

When consumers join or leave a group, Kafka **rebalances** partition assignments.

**Example: Consumer Failure**

```
Before:                     After C2 Crashes:
C1 → P0                    C1 → P0, P1
C2 → P1 (crashes!)         C3 → P2
C3 → P2
                           Partitions automatically reassigned!
```

**Rebalancing Triggers**:
- Consumer joins the group
- Consumer leaves the group (crash or shutdown)
- Consumer is unresponsive (heartbeat timeout)
- New partitions added to topic

**During Rebalancing**:
- No messages are consumed (brief pause)
- Offsets are committed
- Partitions are reassigned

### Multiple Consumer Groups

Different consumer groups can consume the same topic independently:

```
Topic: social-events

Consumer Group: "elasticsearch-group"
  Consumer E1 → P0 (indexes to Elasticsearch)
  Consumer E2 → P1
  Consumer E3 → P2

Consumer Group: "analytics-group"
  Consumer A1 → P0, P1 (calculates statistics)
  Consumer A2 → P2

Consumer Group: "audit-group"
  Consumer U1 → P0, P1, P2 (writes to audit log)

All groups consume the same events independently!
```

**Use Cases**:
- One group indexes to Elasticsearch
- Another group sends notifications
- Another group updates analytics

---

## 7. Offset Management

### What is an Offset?

An **offset** is a unique sequential ID assigned to each event within a partition.

```
Partition 0:
  Offset 0: Event A
  Offset 1: Event B
  Offset 2: Event C
  Offset 3: Event D
  Offset 4: Event E (latest)
           ↑
     Current offset = 4
```

**Properties**:
- Offsets start at 0 and increment by 1
- Offsets are unique within a partition
- Offsets never change (immutable)

### Consumer Offset Tracking

Each consumer group tracks its progress using **committed offsets**.

```
Topic: social-events
Partition 0: [E0, E1, E2, E3, E4, E5, E6, E7, E8, E9]
                                    ↑
                          Last committed offset: 7

Consumer reads E8, E9 next.
```

**Offset Storage**:
- Kafka stores offsets in an internal topic: `__consumer_offsets`
- Offsets are stored per (consumer group, topic, partition)

### Offset Commit Strategies

**1. Auto-Commit (Simplest)**

```yaml
enable-auto-commit: true
auto-commit-interval-ms: 5000  # Commit every 5 seconds
```

- Kafka automatically commits offsets periodically
- **Risk**: May commit before processing completes (at-most-once)

**2. Manual Commit (Most Control)**

```java
@KafkaListener(topics = "social-events")
public void consume(ConsumerRecord<Long, SocialEventAvroModel> record,
                    Acknowledgment ack) {
    try {
        processEvent(record.value());
        ack.acknowledge();  // Manually commit offset
    } catch (Exception e) {
        // Don't commit on failure; event will be reprocessed
        log.error("Failed to process event", e);
    }
}
```

Configuration:
```yaml
enable-auto-commit: false
ack-mode: MANUAL
```

**3. Batch Manual Commit**

```java
@KafkaListener(topics = "social-events", batch = "true")
public void consumeBatch(List<SocialEventAvroModel> events,
                         Acknowledgment ack) {
    try {
        events.forEach(this::processEvent);
        ack.acknowledge();  // Commit after entire batch
    } catch (Exception e) {
        // Entire batch will be reprocessed
    }
}
```

### Offset Reset Behavior

What happens when a consumer starts with no committed offset?

```yaml
auto-offset-reset: earliest  # Start from beginning
# OR
auto-offset-reset: latest    # Start from newest events only
```

**`earliest`**: Read all events from the beginning
**`latest`**: Only read new events (skip historical data)

**Use Cases**:
- **earliest**: New analytics job needs historical data
- **latest**: Real-time notifications (only new events matter)

---

## 8. Delivery Semantics

### At-Most-Once Delivery

Events may be lost, but never processed twice.

```
Read event → Process event → Commit offset
                   ↓ (crash here!)
              Event lost (offset already committed)
```

**Configuration**: Auto-commit enabled, commit before processing
**Use Case**: Non-critical data (metrics, logs)

### At-Least-Once Delivery

Events may be processed multiple times, but never lost.

```
Read event → Process event → Commit offset
        ↓ (crash here!)
   Event reprocessed (offset not yet committed)
```

**Configuration**: Manual commit after processing
**Use Case**: Most applications (with idempotent processing)

**Example**:
```java
@KafkaListener(topics = "social-events")
public void consume(SocialEventAvroModel event, Acknowledgment ack) {
    processEvent(event);    // Process first
    ack.acknowledge();      // Commit after success
}
```

### Exactly-Once Semantics (EOS)

Each event is processed exactly once, no duplicates, no losses.

```
Read event → Process event → Write output → Commit offset
(All in atomic transaction)
```

**Configuration**:
```yaml
spring:
  kafka:
    producer:
      transaction-id-prefix: tx-
    consumer:
      isolation-level: read_committed
```

**Kafka Transactions**:
```java
@Transactional
public void processEvent(SocialEventAvroModel event) {
    // 1. Read from topic
    // 2. Process event
    // 3. Write to output topic
    // 4. Commit offset
    // All succeed or all fail together!
}
```

**Use Case**: Financial transactions, billing systems

### Semantic Comparison

| Semantic | Guarantees | Complexity | Performance | Use Case |
|----------|-----------|------------|-------------|----------|
| At-Most-Once | May lose data | Low | High | Logs, metrics |
| At-Least-Once | May duplicate | Medium | Medium | Most apps |
| Exactly-Once | No loss, no duplicates | High | Lower | Financial |

**Recommendation**: Use **at-least-once** with idempotent processing for most applications.

---

## 9. When to Use Kafka

### Ideal Use Cases

✅ **High-Throughput Event Streaming**
- Millions of events per second
- IoT sensor data
- Log aggregation

✅ **Event Sourcing**
- Store events as source of truth
- Replay events to rebuild state
- Audit trails

✅ **Real-Time Data Pipelines**
- ETL (Extract, Transform, Load)
- Stream data from source to destination
- Data integration

✅ **Microservices Communication**
- Asynchronous, decoupled services
- Event-driven architecture
- Saga patterns

✅ **Stream Processing**
- Real-time analytics
- Fraud detection
- Monitoring and alerting

### When NOT to Use Kafka

❌ **Simple Request-Response**
- Use HTTP/REST for synchronous communication
- Lower latency, simpler

❌ **Small-Scale Applications**
- Kafka has operational overhead
- Use RabbitMQ or AWS SQS for simpler queuing

❌ **Complex Queries**
- Kafka is append-only
- Use a database for ad-hoc queries

❌ **Guaranteed Ordered Processing Across Partitions**
- Ordering only within partitions
- Use a single partition (limits scalability) or ordered queue

### Kafka vs Alternatives

| Feature | Kafka | RabbitMQ | AWS SQS |
|---------|-------|----------|---------|
| **Throughput** | Very High (millions/sec) | Medium | Medium |
| **Durability** | Persistent log | Persistent queues | Persistent |
| **Ordering** | Per partition | Per queue | FIFO queues only |
| **Retention** | Days/weeks | Until consumed | Max 14 days |
| **Complexity** | High | Medium | Low |
| **Best For** | Event streaming | Task queues | Cloud-native queuing |

---

## 10. Summary

### Key Takeaways

✅ **Event-Driven Architecture** enables decoupled, scalable, resilient systems
✅ **Apache Kafka** is a distributed event streaming platform for high-throughput workloads
✅ **Topics** organize events by category; **partitions** enable parallelism and ordering
✅ **Producers** publish events; **consumers** subscribe and process events
✅ **Consumer groups** allow parallel processing with automatic load balancing
✅ **Offsets** track consumer progress; commit strategies affect delivery semantics
✅ **At-least-once** delivery is most common, with idempotent processing

### Terminology Recap

| Term | Definition |
|------|------------|
| **Event** | An immutable record of something that happened |
| **Topic** | A category/feed for events |
| **Partition** | An ordered, append-only log within a topic |
| **Broker** | A Kafka server that stores and serves data |
| **Producer** | Application that publishes events to topics |
| **Consumer** | Application that reads events from topics |
| **Consumer Group** | Set of consumers sharing partition consumption |
| **Offset** | Sequential ID for each event in a partition |
| **Replication** | Copying partitions across brokers for fault tolerance |

### Conceptual Model

```
┌─────────────────────────────────────────────────────────────────┐
│                    KAFKA ECOSYSTEM                               │
│                                                                  │
│  Producers                Kafka Cluster              Consumers  │
│     ↓                          ↓                         ↓      │
│  [Events] ────────▶  [Topics/Partitions]  ────────▶  [Apps]   │
│                       [Replicated]                              │
│                       [Durable]                                 │
│                       [Ordered]                                 │
│                                                                  │
│  Configuration:      Managed by:          Tracked by:           │
│  - Serialization     - Brokers            - Offsets             │
│  - Acks              - ZooKeeper/KRaft    - Consumer Groups     │
│  - Batching          - Replication        - Commits             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Next Steps

Now that you understand Kafka fundamentals, you're ready to explore the project architecture!

👉 **[Proceed to Module 2: Project Architecture & Setup](./02-project-architecture.md)**

---

## Hands-On Exercises

### Exercise 1: Kafka Basics Quiz

Test your understanding:

1. What is the maximum number of consumers in a consumer group that can actively read from a topic with 5 partitions?
2. If `acks=all`, how many brokers must acknowledge a write if replication factor is 3?
3. What happens to offsets when a consumer crashes before committing?
4. Can events with different keys end up in the same partition?
5. What's the difference between `auto-offset-reset: earliest` and `latest`?

**[View Answers](./exercises/module-01-answers.md)**

### Exercise 2: Design an Event-Driven System

Design a simple e-commerce system with:
- Services: Order Service, Inventory Service, Notification Service
- Events: OrderPlaced, InventoryReserved, OrderShipped
- Topics: How many? Which services produce/consume which topics?

**[See Solution](./exercises/module-01-design.md)**

### Exercise 3: Calculate Throughput

Given:
- 3 partitions
- 3 consumers (one per partition)
- Each consumer processes 1000 events/second

Questions:
1. What's the total throughput?
2. If you add 2 more partitions but no new consumers, what changes?
3. What if you add 2 more consumers but no new partitions?

**[View Solution](./exercises/module-01-throughput.md)**

---

## Additional Resources

- [Kafka Documentation: Introduction](https://kafka.apache.org/intro)
- [Confluent: Kafka 101](https://developer.confluent.io/learn-kafka/)
- [Event-Driven Architecture (Martin Fowler)](https://martinfowler.com/articles/201701-event-driven.html)
- [Kafka Internals (YouTube)](https://www.youtube.com/watch?v=udnX21__SuU)

---

**Module Progress**: 1 of 10 complete
