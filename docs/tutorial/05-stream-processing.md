# Module 5: Real-Time Stream Processing with Kafka Streams

**Duration**: 90 minutes
**Difficulty**: Intermediate to Advanced
**Prerequisites**: Modules 1-4 completed

---

## Learning Objectives

By the end of this module, you will understand:

✅ Kafka Streams fundamentals and architecture
✅ Stream topology design patterns
✅ Stateless transformations (filter, map, flatMap)
✅ Stateful operations (aggregations, windowing)
✅ Time semantics and windowing strategies
✅ KStream vs KTable vs GlobalKTable
✅ Building real-time analytics pipelines

---

## Table of Contents

1. [Kafka Streams Overview](#1-kafka-streams-overview)
2. [Stream Topology Concepts](#2-stream-topology-concepts)
3. [Stateless Transformations](#3-stateless-transformations)
4. [Stateful Operations](#4-stateful-operations)
5. [Time and Windowing](#5-time-and-windowing)
6. [Implementation: Word Count Analytics](#6-implementation-word-count-analytics)
7. [KStream vs KTable](#7-kstream-vs-ktable)
8. [Testing Stream Applications](#8-testing-stream-applications)
9. [Summary](#9-summary)

---

## 1. Kafka Streams Overview

### What is Kafka Streams?

**Kafka Streams** is a client library for building stream processing applications that:
- Process data stored in Kafka topics
- Transform, aggregate, and enrich data in real-time
- Produce results back to Kafka topics
- Run as standard Java applications (no separate cluster needed)

### Kafka Streams vs Other Stream Processing

| Feature | Kafka Streams | Apache Flink | Spark Streaming |
|---------|---------------|--------------|-----------------|
| **Deployment** | Library (JVM app) | Cluster required | Cluster required |
| **Latency** | Milliseconds | Milliseconds | Seconds (micro-batch) |
| **State Management** | Built-in (RocksDB) | Built-in | External store |
| **Exactly-Once** | Yes | Yes | Yes |
| **Complexity** | Low | Medium | High |
| **Best For** | Kafka-native apps | Complex CEP | Batch + Streaming |

### Architecture

```
┌────────────────────────────────────────────────────────────┐
│           Kafka Streams Application                         │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │  Stream Topology (DAG)                             │   │
│  │                                                     │   │
│  │   Source → Transform → Aggregate → Sink            │   │
│  │     ↓         ↓           ↓          ↓             │   │
│  │   Read     Filter      Count       Write           │   │
│  │   Topic    Map         Join        Topic           │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │  State Stores (RocksDB)                            │   │
│  │  - Aggregations                                    │   │
│  │  - Joins                                           │   │
│  │  - Windowed data                                   │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
└────────────────────────────────────────────────────────────┘
         ↑                                     ↓
    Input Topics                         Output Topics
    (Kafka Cluster)                      (Kafka Cluster)
```

### Key Concepts

**1. Stream**: Unbounded, continuously updating dataset
**2. Topology**: Directed acyclic graph (DAG) of processing nodes
**3. Processor**: Node that transforms data (filter, map, aggregate)
**4. State Store**: Local database for stateful operations (RocksDB)
**5. Changelog Topic**: Kafka topic backing the state store

---

## 2. Stream Topology Concepts

### Stream Processing DAG

```
┌─────────────────────────────────────────────────────────────┐
│                    STREAM TOPOLOGY                           │
│                                                              │
│  Source Topic: social-events                                │
│         ↓                                                    │
│    [KStream: all events]                                    │
│         ↓                                                    │
│    ┌─────────────────────────┐                              │
│    │  Filter: text != null   │  (Stateless)                │
│    └─────────────────────────┘                              │
│         ↓                                                    │
│    [KStream: filtered events]                               │
│         ↓                                                    │
│    ┌──────────────────────────────────┐                     │
│    │  Branch to multiple paths        │                     │
│    └──────────────────────────────────┘                     │
│         ↓                   ↓                                │
│    Path 1               Path 2                              │
│         ↓                   ↓                                │
│    To topic           FlatMap: extract words                │
│    "filtered"              ↓                                │
│                       GroupBy: word                          │
│                            ↓                                │
│                       Window: 5-min tumbling                │
│                            ↓                                │
│                       Count (Stateful)                      │
│                            ↓                                │
│                       To topic "word-count"                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Topology Building Blocks

**1. Source Processor**: Read from topic
```java
StreamsBuilder builder = new StreamsBuilder();
KStream<Long, SocialEventAvroModel> stream =
    builder.stream("social-events");
```

**2. Transform Processor**: Modify stream
```java
KStream<Long, SocialEventAvroModel> filtered =
    stream.filter((key, value) -> value.getText() != null);
```

**3. Sink Processor**: Write to topic
```java
filtered.to("social-events-filtered");
```

---

## 3. Stateless Transformations

### filter: Keep Events Matching Predicate

```java
KStream<Long, SocialEventAvroModel> stream = builder.stream("social-events");

// Filter: only events with text content
KStream<Long, SocialEventAvroModel> filtered =
    stream.filter((key, value) ->
        value.getText() != null &&
        !value.getText().isEmpty()
    );

// Example:
// Input:  {userId: 1, text: "Hello"}    → passes
// Input:  {userId: 2, text: null}       → filtered out
// Input:  {userId: 3, text: ""}         → filtered out
```

**Use Cases**:
- Remove events that don't meet criteria
- Data quality filtering
- Conditional routing

### map: Transform Each Event

```java
// Map: Extract text length
KStream<Long, Integer> textLengths =
    stream.map((key, value) ->
        KeyValue.pair(key, value.getText().length())
    );

// Example:
// Input:  {userId: 1, text: "Hello Kafka"}
// Output: {userId: 1, value: 11}
```

**Use Cases**:
- Data transformation
- Format conversion
- Enrichment

### mapValues: Transform Only Values (Key Unchanged)

```java
// More efficient than map when key doesn't change
KStream<Long, String> upperCaseTexts =
    stream.mapValues(value -> value.getText().toUpperCase());

// Example:
// Input:  {userId: 1, text: "hello"}
// Output: {userId: 1, value: "HELLO"}
```

### flatMap: One-to-Many Transformation

```java
// FlatMap: Extract all words from text
KStream<String, String> words =
    stream.flatMap((key, value) -> {
        String text = value.getText();
        String[] words = text.toLowerCase().split("\\W+");

        List<KeyValue<String, String>> result = new ArrayList<>();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.add(KeyValue.pair(word, word));
            }
        }
        return result;
    });

// Example:
// Input:  {userId: 1, text: "Hello Kafka Streams"}
// Output: {key: "hello", value: "hello"}
//         {key: "kafka", value: "kafka"}
//         {key: "streams", value: "streams"}
```

**Use Cases**:
- Tokenization
- Parsing multi-value fields
- Event explosion

### branch: Split Stream into Multiple Branches

```java
// Branch: Route events by criteria
KStream<Long, SocialEventAvroModel>[] branches =
    stream.branch(
        (key, value) -> value.getText().contains("urgent"),  // Branch 0
        (key, value) -> value.getText().contains("error"),   // Branch 1
        (key, value) -> true                                  // Branch 2: default
    );

KStream<Long, SocialEventAvroModel> urgentEvents = branches[0];
KStream<Long, SocialEventAvroModel> errorEvents = branches[1];
KStream<Long, SocialEventAvroModel> normalEvents = branches[2];

// Route to different topics
urgentEvents.to("urgent-events");
errorEvents.to("error-events");
normalEvents.to("normal-events");
```

---

### 🎯 Quick Exercise: Build a Stream Transformation Pipeline

**Time**: 15 minutes | **Difficulty**: Intermediate

**Scenario**: You're processing order events for an e-commerce platform.

**Input Event**:
```json
{
  "orderId": "ORD-12345",
  "customerId": "CUST-789",
  "items": "laptop,mouse,keyboard",
  "totalAmount": 1299.99,
  "status": "PENDING"
}
```

**Task**: Build a stream processing pipeline that:

1. **Filter**: Only keep orders with `totalAmount > 100`
2. **MapValues**: Convert `totalAmount` to integer (round up)
3. **FlatMapValues**: Split `items` string into individual items
4. **Branch**: Route orders by amount:
   - High value: `totalAmount >= 1000` → `high-value-orders`
   - Medium value: `totalAmount >= 500` → `medium-value-orders`
   - Regular: `totalAmount < 500` → `regular-orders`

**Your Turn**: Write the Kafka Streams DSL code.

**Template**:
```java
KStream<String, Order> orders = builder.stream("orders");

// Step 1: Filter
KStream<String, Order> filtered = orders.filter(???);

// Step 2: Map values
KStream<String, Order> mapped = filtered.mapValues(???);

// Step 3: FlatMap (create item stream)
KStream<String, String> items = mapped.flatMapValues(???);

// Step 4: Branch
KStream<String, Order>[] branches = mapped.branch(???);
```

**Bonus**: How would you count items per category in 1-hour windows?

**Solution**:
<details>
<summary>Click to reveal solution</summary>

```java
// Input stream
KStream<String, Order> orders = builder.stream(
    "orders",
    Consumed.with(Serdes.String(), orderSerde)
);

// Step 1: Filter orders > $100
KStream<String, Order> filtered = orders.filter(
    (key, order) -> order.getTotalAmount() > 100
);

// Step 2: Map values - round up amount
KStream<String, Order> mapped = filtered.mapValues(order -> {
    Order updated = order.copy();
    updated.setTotalAmount(Math.ceil(order.getTotalAmount()));
    return updated;
});

// Step 3: FlatMap - extract individual items
KStream<String, String> items = mapped.flatMapValues(order -> {
    String itemsString = order.getItems();
    String[] itemArray = itemsString.split(",");

    List<String> itemList = Arrays.stream(itemArray)
        .map(String::trim)
        .collect(Collectors.toList());

    return itemList;
});

// Send items to separate topic
items.to("order-items", Produced.with(Serdes.String(), Serdes.String()));

// Step 4: Branch by order value
KStream<String, Order>[] branches = mapped.branch(
    (key, order) -> order.getTotalAmount() >= 1000,  // Branch 0: High
    (key, order) -> order.getTotalAmount() >= 500,   // Branch 1: Medium
    (key, order) -> true                             // Branch 2: Regular
);

KStream<String, Order> highValueOrders = branches[0];
KStream<String, Order> mediumValueOrders = branches[1];
KStream<String, Order> regularOrders = branches[2];

// Route to different topics
highValueOrders.to("high-value-orders");
mediumValueOrders.to("medium-value-orders");
regularOrders.to("regular-orders");

// Log statistics
highValueOrders.foreach((key, order) ->
    log.info("High value order: ${}", order.getTotalAmount())
);
```

**Bonus: Count Items per Category in 1-Hour Windows**

```java
// Assuming items have format "category:item" like "electronics:laptop"
KStream<String, String> categorizedItems = mapped.flatMapValues(order -> {
    String[] items = order.getItems().split(",");
    return Arrays.stream(items)
        .map(String::trim)
        .collect(Collectors.toList());
});

// Extract category as key
KStream<String, String> withCategoryKey = categorizedItems
    .map((key, item) -> {
        String category = item.split(":")[0];  // "electronics"
        return KeyValue.pair(category, item);
    });

// Count by category in 1-hour tumbling windows
KTable<Windowed<String>, Long> categoryCounts =
    withCategoryKey
        .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
        .count();

// Output to topic
categoryCounts.toStream()
    .map((windowedKey, count) -> {
        String category = windowedKey.key();
        long windowStart = windowedKey.window().start();
        String outputKey = category + ":" + windowStart;
        return KeyValue.pair(outputKey, count);
    })
    .to("category-counts-hourly");
```

**Expected Output**:
```
Input: "electronics:laptop,electronics:mouse,furniture:desk"

After FlatMap:
- "electronics:laptop"
- "electronics:mouse"
- "furniture:desk"

After Category Grouping (1-hour window):
- "electronics:1700496000000" → 2
- "furniture:1700496000000" → 1
```

**Key Takeaways**:
- **filter**: Removes unwanted events early (improves performance)
- **mapValues**: Transforms values without repartitioning (efficient)
- **flatMapValues**: One-to-many transformation (explodes events)
- **branch**: Routes to different paths based on conditions
- **Combining operations**: Build complex pipelines from simple operations!
</details>

---

## 4. Stateful Operations

### groupBy: Group Events for Aggregation

```java
// Group by word
KGroupedStream<String, String> groupedByWord =
    words.groupByKey();

// Or group by a different key
KGroupedStream<String, SocialEventAvroModel> groupedByUser =
    stream.groupBy(
        (key, value) -> String.valueOf(value.getUserId()),
        Grouped.with(Serdes.String(), avroSerde)
    );
```

### count: Count Events per Key

```java
// Count occurrences of each word
KTable<String, Long> wordCounts =
    words.groupByKey()
         .count();

// Example state:
// "kafka"  → 42
// "spring" → 31
// "boot"   → 28
```

### aggregate: Custom Aggregation

```java
// Aggregate: Count events per user
KTable<String, Long> eventsPerUser =
    stream.groupBy(
        (key, value) -> String.valueOf(value.getUserId()),
        Grouped.with(Serdes.String(), avroSerde)
    )
    .aggregate(
        () -> 0L,                                    // Initializer
        (aggKey, newValue, aggValue) -> aggValue + 1, // Adder
        Materialized.with(Serdes.String(), Serdes.Long())
    );

// Example state:
// "user-1" → 15
// "user-2" → 23
// "user-3" → 8
```

### reduce: Combine Values

```java
// Reduce: Keep latest event per user
KTable<Long, SocialEventAvroModel> latestEventPerUser =
    stream.groupByKey()
          .reduce((oldValue, newValue) -> newValue);

// Example:
// User 1: Event A (time: 100) → Event B (time: 200)
// Result: Event B (latest)
```

---

## 5. Time and Windowing

### Time Semantics

**1. Event Time**: When the event occurred (from event data)
```java
// Use timestamp from event
.windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
```

**2. Processing Time**: When the event is processed (wall-clock time)
```java
// Use current system time
```

**3. Ingestion Time**: When Kafka broker received the event (timestamp from broker)

### Windowing Strategies

**1. Tumbling Windows**: Fixed-size, non-overlapping

```
Time:     0    5    10   15   20   25   30
          ├────┼────┼────┼────┼────┼────┤
Window 1: [----]
Window 2:      [----]
Window 3:           [----]
Window 4:                [----]

Each event belongs to exactly one window.
```

```java
TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5));

// Window 1: 00:00-00:05
// Window 2: 00:05-00:10
// Window 3: 00:10-00:15
```

**2. Hopping Windows**: Fixed-size, overlapping

```
Time:     0    5    10   15   20   25   30
          ├────┼────┼────┼────┼────┼────┤
Window 1: [----------]
Window 2:      [----------]
Window 3:           [----------]

Each event belongs to multiple windows.
```

```java
TimeWindows.ofSizeAndGrace(
    Duration.ofMinutes(10),  // Window size
    Duration.ofMinutes(5)    // Advance interval
);

// Window 1: 00:00-00:10
// Window 2: 00:05-00:15
// Window 3: 00:10-00:20
```

**3. Sliding Windows**: Variable-size, based on time difference

```java
// Used with joins
JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5));

// Event A at time T will join with Event B if:
// B.timestamp is in [T-5min, T+5min]
```

**4. Session Windows**: Dynamic size, based on inactivity gap

```
User Activity:
Event 1: 00:00
Event 2: 00:02 (gap: 2min)
Event 3: 00:04 (gap: 2min)
--- 10 min gap ---
Event 4: 00:15

With 5-min inactivity gap:
Session 1: [00:00 - 00:04]  (Events 1-3)
Session 2: [00:15 - 00:15]  (Event 4)
```

```java
SessionWindows.ofInactivityGapWithNoGrace(Duration.ofMinutes(5));
```

### Grace Period

**Problem**: Late-arriving events

```
Window: 00:00-00:05
Event A: timestamp = 00:03, arrives at 00:04 → in window
Event B: timestamp = 00:04, arrives at 00:07 → late!
```

**Solution**: Grace period allows late events

```java
TimeWindows.ofSizeAndGrace(
    Duration.ofMinutes(5),  // Window size
    Duration.ofMinutes(2)   // Grace period
);

// Window 00:00-00:05 accepts events until 00:07
```

---

### 🎯 Quick Exercise: Window Calculations

**Time**: 12 minutes | **Difficulty**: Intermediate

**Scenario**: You're processing user activity events with different windowing strategies.

**Given Events**:
```
Event A: timestamp = 10:00:00
Event B: timestamp = 10:02:30
Event C: timestamp = 10:05:00
Event D: timestamp = 10:07:00
Event E: timestamp = 10:12:00
```

**Question 1: Tumbling Windows** (5-minute windows, no grace period)

Windows: `[10:00-10:05), [10:05-10:10), [10:10-10:15)`

Which events fall into which windows?
```
Window 1 (10:00-10:05): ?
Window 2 (10:05-10:10): ?
Window 3 (10:10-10:15): ?
```

**Question 2: Hopping Windows** (10-minute size, 5-minute advance)

Windows: `[10:00-10:10), [10:05-10:15), [10:10-10:20)`

Which events belong to which windows? (Events can be in multiple windows!)
```
Window 1 (10:00-10:10): ?
Window 2 (10:05-10:15): ?
Window 3 (10:10-10:20): ?
```

**Question 3: Session Windows** (5-minute inactivity gap)

Group events into sessions. A new session starts if gap > 5 minutes.
```
Session 1: ?
Session 2: ?
```

**Question 4: Grace Period**

Event F arrives late:
```
Event F: timestamp = 10:04:00, arrives at 10:11:00 (7 minutes late)
```

With tumbling 5-minute windows and 2-minute grace period:
- Window for Event F: `[10:00-10:05)`
- Grace period ends: `10:07:00`
- Event arrives: `10:11:00`

Is Event F accepted or dropped?

**Answers**:
<details>
<summary>Click to reveal answers</summary>

**Question 1: Tumbling Windows**
```
Window 1 [10:00-10:05): Event A (10:00:00), Event B (10:02:30)
Window 2 [10:05-10:10): Event C (10:05:00), Event D (10:07:00)
Window 3 [10:10-10:15): Event E (10:12:00)
```

**Key**: Each event belongs to exactly ONE window based on timestamp.

---

**Question 2: Hopping Windows**
```
Window 1 [10:00-10:10): Event A (10:00:00), Event B (10:02:30),
                        Event C (10:05:00), Event D (10:07:00)

Window 2 [10:05-10:15): Event C (10:05:00), Event D (10:07:00),
                        Event E (10:12:00)

Window 3 [10:10-10:20): Event E (10:12:00)
```

**Key**: Events can belong to MULTIPLE overlapping windows!
- Event C and D appear in both Window 1 and Window 2
- Event E appears in both Window 2 and Window 3

**Use Case**: Moving averages, trend detection

---

**Question 3: Session Windows**
```
Gap analysis:
A → B: 2.5 min gap (< 5 min, same session)
B → C: 2.5 min gap (< 5 min, same session)
C → D: 2 min gap (< 5 min, same session)
D → E: 5 min gap (= 5 min, borderline, typically new session)

Session 1 [10:00:00 - 10:07:00]: Event A, B, C, D
Session 2 [10:12:00 - 10:12:00]: Event E
```

**Key**: Dynamic window size based on user inactivity!

**Configuration**:
```java
SessionWindows.ofInactivityGapWithNoGrace(Duration.ofMinutes(5))
```

**Use Case**: User browsing sessions, click tracking

---

**Question 4: Grace Period**

**Answer**: Event F is **DROPPED** ❌

**Why**:
```
Event F timestamp: 10:04:00
  → Should go to window [10:00-10:05)

Window closes: 10:05:00
Grace period: +2 minutes
Final deadline: 10:07:00

Event arrives: 10:11:00 (4 minutes after deadline!)
  → Too late, dropped
```

**What if Event F arrived at 10:06:00?**
```
Arrival: 10:06:00 (within grace period)
  → ACCEPTED ✅
  → Added to window [10:00-10:05)
  → Window result updated
```

**Configuration**:
```java
TimeWindows.ofSizeAndGrace(
    Duration.ofMinutes(5),  // Window size
    Duration.ofMinutes(2)   // Grace period
);
```

**Key Insight**: Grace periods handle out-of-order events, but have limits!
</details>

---

## 6. Implementation: Word Count Analytics

### Complete Topology

**File**: `kafka-streams-service/src/main/java/.../SocialEventStreamsTopology.java`

```java
@Configuration
@Slf4j
public class SocialEventStreamsTopology {

    private final KafkaStreamsConfigData kafkaStreamsConfigData;
    private final SpecificAvroSerde<SocialEventAvroModel> avroSerde;

    @Bean
    public KStream<Long, SocialEventAvroModel> buildTopology(
            StreamsBuilder streamsBuilder) {

        // 1. Source: Read from social-events topic
        KStream<Long, SocialEventAvroModel> stream =
            streamsBuilder.stream(
                kafkaStreamsConfigData.getInputTopicName(),
                Consumed.with(Serdes.Long(), avroSerde)
            );

        log.info("Created source stream from topic: {}",
            kafkaStreamsConfigData.getInputTopicName());

        // 2. Filter: Only events with text
        KStream<Long, SocialEventAvroModel> filteredStream =
            stream.filter((key, value) -> {
                boolean hasText = value.getText() != null &&
                                !value.getText().toString().isEmpty();
                if (hasText) {
                    log.debug("Event passed filter: {}", value.getId());
                } else {
                    log.debug("Event filtered out: {}", value.getId());
                }
                return hasText;
            });

        // 3. Write filtered events to topic
        filteredStream.to(
            "social-events-filtered",
            Produced.with(Serdes.Long(), avroSerde)
        );

        // 4. Extract words (flatMap)
        KStream<String, String> words =
            filteredStream.flatMapValues(value -> {
                String text = value.getText().toString();
                String[] wordArray = text.toLowerCase()
                    .split("\\W+");

                List<String> validWords = new ArrayList<>();
                for (String word : wordArray) {
                    if (!word.isEmpty() && word.length() > 2) {
                        validWords.add(word);
                    }
                }

                log.debug("Extracted {} words from event {}",
                    validWords.size(), value.getId());
                return validWords;
            })
            .selectKey((key, word) -> word);  // Re-key by word

        // 5. Group by word
        KGroupedStream<String, String> groupedWords =
            words.groupByKey(
                Grouped.with(Serdes.String(), Serdes.String())
            );

        // 6. Window: 5-minute tumbling windows
        TimeWindowedKStream<String, String> windowedWords =
            groupedWords.windowedBy(
                TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5))
            );

        // 7. Count: Aggregate word counts per window
        KTable<Windowed<String>, Long> wordCounts =
            windowedWords.count(
                Materialized.as("word-counts-store")
            );

        // 8. Transform to output format and write to topic
        wordCounts.toStream()
            .map((windowedKey, count) -> {
                String word = windowedKey.key();
                long windowStart = windowedKey.window().start();
                long windowEnd = windowedKey.window().end();

                String outputKey = String.format("%s:%d-%d",
                    word, windowStart, windowEnd);

                log.debug("Word count: {} appeared {} times in window {}-{}",
                    word, count, windowStart, windowEnd);

                return KeyValue.pair(outputKey, count);
            })
            .to("social-events-word-count",
                Produced.with(Serdes.String(), Serdes.Long()));

        log.info("Stream topology built successfully");

        return stream;
    }
}
```

### Step-by-Step Walkthrough

**Step 1: Source**
```java
KStream<Long, SocialEventAvroModel> stream =
    streamsBuilder.stream("social-events");

// Reads all events from topic
// Key: userId (Long)
// Value: SocialEventAvroModel (Avro)
```

**Step 2: Filter**
```java
KStream<Long, SocialEventAvroModel> filtered =
    stream.filter((key, value) ->
        value.getText() != null && !value.getText().isEmpty()
    );

// Input:  100 events
// Output: 85 events (15 had no text)
```

**Step 3: Sink Filtered Events**
```java
filtered.to("social-events-filtered");

// Topic: social-events-filtered
// Contains only events with text
// Other services can consume this refined stream
```

**Step 4: Extract Words**
```java
KStream<String, String> words =
    filtered.flatMapValues(value -> {
        String[] words = value.getText().toLowerCase().split("\\W+");
        return Arrays.asList(words).stream()
            .filter(w -> w.length() > 2)  // Min 3 characters
            .collect(Collectors.toList());
    })
    .selectKey((key, word) -> word);

// Input:  {userId: 1, text: "Learning Kafka Streams"}
// Output: {key: "learning", value: "learning"}
//         {key: "kafka", value: "kafka"}
//         {key: "streams", value: "streams"}
```

**Step 5-6: Group and Window**
```java
KGroupedStream<String, String> grouped = words.groupByKey();

TimeWindowedKStream<String, String> windowed =
    grouped.windowedBy(
        TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5))
    );

// Events grouped by word, then by 5-minute time windows
```

**Step 7: Count**
```java
KTable<Windowed<String>, Long> counts = windowed.count();

// State Store: word-counts-store
// Key: Windowed<"kafka"> (word + window)
// Value: 15 (count in this window)
```

**Step 8: Output**
```java
counts.toStream()
    .map((windowedKey, count) ->
        KeyValue.pair(
            windowedKey.key() + ":" +
                windowedKey.window().start() + "-" +
                windowedKey.window().end(),
            count
        )
    )
    .to("social-events-word-count");

// Topic: social-events-word-count
// Key: "kafka:1700500000000-1700500300000"
// Value: 15
```

### Configuration

```yaml
kafka-streams-config:
  application-id: social-events-streams-app
  input-topic-name: social-events
  state-dir: /tmp/kafka-streams
  replication-factor: 3
  num-stream-threads: 3

  # Optimization
  commit-interval-ms: 10000         # Commit every 10 seconds
  cache-max-bytes-buffering: 10485760  # 10 MB cache
```

### State Store Persistence

```
/tmp/kafka-streams/
  └── social-events-streams-app/
      └── 0_0/
          └── rocksdb/
              └── word-counts-store/
                  ├── 000001.log
                  ├── CURRENT
                  ├── MANIFEST-000002
                  └── ...

Backed by changelog topic: social-events-streams-app-word-counts-store-changelog
```

**Benefits**:
- **Fault Tolerance**: If app crashes, restore from changelog
- **Scalability**: Each partition has its own state store
- **Fast Queries**: Local RocksDB reads (no network)

---

## 7. KStream vs KTable

### KStream: Event Stream

- **Represents**: Unbounded stream of events
- **Semantics**: Insert-only (append)
- **Example**: Click events, log entries

```java
KStream<String, String> clicks =
    builder.stream("page-clicks");

// Each event is independent
// Event 1: {user: "alice", page: "home"}
// Event 2: {user: "alice", page: "about"}
// Both events exist independently
```

### KTable: Changelog Stream

- **Represents**: Latest state per key
- **Semantics**: Upsert (insert/update)
- **Example**: User profiles, inventory

```java
KTable<String, String> userProfiles =
    builder.table("user-profiles");

// Latest value per key
// Event 1: {key: "alice", value: "profile-v1"}
// Event 2: {key: "alice", value: "profile-v2"}
// KTable only keeps: {key: "alice", value: "profile-v2"}
```

### GlobalKTable: Replicated KTable

- **Represents**: Full table replicated to all instances
- **Use Case**: Reference data (countries, products)

```java
GlobalKTable<String, String> countries =
    builder.globalTable("countries");

// Every stream instance has full copy
// Enables lookup joins without partitioning
```

### Comparison

| Feature | KStream | KTable | GlobalKTable |
|---------|---------|--------|--------------|
| **Type** | Event stream | Changelog | Replicated table |
| **Updates** | All events | Latest per key | Latest per key |
| **Storage** | No state | Local state | Full replication |
| **Joins** | Partitioned | Partitioned | Non-partitioned |
| **Use Case** | Events | Aggregations | Reference data |

---

## 8. Testing Stream Applications

### Unit Testing with TopologyTestDriver

```java
@Test
public void testWordCountTopology() {
    // 1. Build topology
    StreamsBuilder builder = new StreamsBuilder();
    KStream<Long, SocialEventAvroModel> stream =
        topology.buildTopology(builder);

    // 2. Create test driver
    Topology topology = builder.build();
    TopologyTestDriver testDriver =
        new TopologyTestDriver(topology, streamsConfig());

    // 3. Create input/output test topics
    TestInputTopic<Long, SocialEventAvroModel> inputTopic =
        testDriver.createInputTopic(
            "social-events",
            Serdes.Long().serializer(),
            avroSerde.serializer()
        );

    TestOutputTopic<String, Long> outputTopic =
        testDriver.createOutputTopic(
            "social-events-word-count",
            Serdes.String().deserializer(),
            Serdes.Long().deserializer()
        );

    // 4. Send test events
    SocialEventAvroModel event1 = createEvent("Hello Kafka Kafka");
    inputTopic.pipeInput(1L, event1);

    // 5. Verify output
    KeyValue<String, Long> output1 = outputTopic.readKeyValue();
    assertEquals("hello", extractWord(output1.key));
    assertEquals(1L, output1.value);

    KeyValue<String, Long> output2 = outputTopic.readKeyValue();
    assertEquals("kafka", extractWord(output2.key));
    assertEquals(2L, output2.value);  // "kafka" appeared twice

    // 6. Cleanup
    testDriver.close();
}
```

### Integration Testing

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"social-events", "social-events-word-count"})
public class StreamsIntegrationTest {

    @Autowired
    private KafkaTemplate<Long, SocialEventAvroModel> producer;

    @Autowired
    private KafkaStreamsConfiguration streamsConfig;

    @Test
    public void testEndToEndProcessing() throws Exception {
        // Send event
        SocialEventAvroModel event = createEvent("Test Kafka Streams");
        producer.send("social-events", 1L, event).get();

        // Wait for processing
        Thread.sleep(5000);

        // Verify output topic
        ConsumerRecords<String, Long> records = consumeFromTopic("social-events-word-count");
        assertTrue(records.count() > 0);

        // Verify word counts
        Map<String, Long> wordCounts = extractWordCounts(records);
        assertEquals(1L, wordCounts.get("test"));
        assertEquals(1L, wordCounts.get("kafka"));
        assertEquals(1L, wordCounts.get("streams"));
    }
}
```

---

## 9. Summary

### Key Takeaways

✅ **Kafka Streams** enables real-time stream processing as a library (no cluster)
✅ **Topologies** define the processing flow (DAG of transformations)
✅ **Stateless operations** (filter, map, flatMap) transform without storing state
✅ **Stateful operations** (count, aggregate, join) use local state stores
✅ **Windowing** groups events by time (tumbling, hopping, sliding, session)
✅ **KStream** represents event streams, **KTable** represents changelog streams
✅ **State stores** (RocksDB) enable fault-tolerant, scalable stateful processing

### Stream Processing Patterns

```
1. Filtering: Remove unwanted events
2. Transformation: Convert event format
3. Enrichment: Join with reference data
4. Aggregation: Count, sum, avg over windows
5. Routing: Branch to different topics
6. Deduplication: Remove duplicates
7. Sessionization: Group by user session
```

### Best Practices

✅ **Use meaningful application IDs** (unique per topology)
✅ **Configure grace periods** for late events
✅ **Choose appropriate window sizes** (balance latency vs completeness)
✅ **Monitor lag** on changelog topics
✅ **Test with TopologyTestDriver** before deploying
✅ **Use KTables for latest state** (avoid redundant aggregations)
✅ **Partition reference data** with GlobalKTable for joins

---

## Next Steps

You've mastered stream processing! Now learn how to index events for full-text search.

👉 **[Proceed to Module 6: Elasticsearch Integration](./06-elasticsearch-integration.md)**

---

## Hands-On Exercises

### Exercise 1: Implement Sentiment Analysis

**Task**: Add sentiment field to events (POSITIVE, NEGATIVE, NEUTRAL).

**Steps**:
1. Define sentiment enum in Avro schema
2. Create sentiment analysis function (keyword-based)
3. Add `mapValues` to enrich events with sentiment
4. Count events per sentiment per 5-min window
5. Write to `social-events-sentiment-count` topic

### Exercise 2: Top N Words

**Task**: Find top 10 most frequent words per hour.

**Hints**:
- Use 1-hour tumbling windows
- Count words as before
- Sort by count (descending)
- Keep top 10
- Use `suppress()` to emit only at window end

### Exercise 3: User Activity Sessions

**Task**: Group user events into sessions (5-min inactivity gap).

**Steps**:
1. Group by userId
2. Use session windows (5-min gap)
3. Count events per session
4. Calculate average events per session

---

**Module Progress**: 5 of 10 complete
