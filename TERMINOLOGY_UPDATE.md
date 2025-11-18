# Terminology Update - Version 3.1.0

## Overview
This release updates the project terminology to reflect its true nature as a generic event stream simulator, removing Twitter-specific references.

## Module & Directory Changes

### Renamed
| Old Name | New Name |
|----------|----------|
| `twitter-to-kafka-service/` | `event-stream-service/` |

## Java Classes & Interfaces

### Renamed Classes
| Old Name | New Name |
|----------|----------|
| `TwitterToKafkaServiceApplication` | `EventStreamServiceApplication` |
| `TwitterKafkaProducer` | `AvroKafkaProducer` (generic) |
| `TwitterToKafkaServiceConfigData` | `EventStreamConfigData` |
| `TwitterToKafkaServiceException` | `EventStreamServiceException` |

### Package Changes
| Old Package | New Package |
|-------------|-------------|
| `com.microservices.demo.twitter.to.kafka.service` | `com.microservices.demo.event.stream.service` |

## Avro Schema Changes

### Schema Files
| Old File | New File |
|----------|----------|
| `twitter.avsc` | `social-event.avsc` |

### Avro Model Classes
| Old Name | New Name |
|----------|----------|
| `TwitterAvroModel` | `SocialEventAvroModel` |

## Configuration Changes

### application.yml
| Old Property | New Property |
|--------------|--------------|
| `twitter-to-kafka-service` | `event-stream-service` |
| `twitter-keywords` | `event-keywords` |
| `enable-mock-tweets` | `enable-mock-events` |
| `mock-min-tweet-length` | `mock-min-message-length` |
| `mock-max-tweet-length` | `mock-max-message-length` |

### Kafka Topics
| Old Topic | New Topic |
|-----------|-----------|
| `twitter-topic` | `social-events` |

## Log Messages & Documentation

### Terminology Updates
- "Twitter" → "Event" or "Social Event"
- "Tweet" → "Event" or "Message"
- "Twitter keywords" → "Event keywords" or "Message topics"
- "Mock tweets" → "Mock events"

## Migration Guide

### For Existing Deployments

**1. Update Configuration**
```yaml
# Old
twitter-to-kafka-service:
  twitter-keywords:
    - Java

# New
event-stream-service:
  event-keywords:
    - Java
```

**2. Update Kafka Topics**
- Old topic: `twitter-topic`
- New topic: `social-events`
- Data format remains compatible (Avro schema fields unchanged)

**3. Update Application Startup**
```bash
# Old
cd twitter-to-kafka-service && mvn spring-boot:run

# New
cd event-stream-service && mvn spring-boot:run
```

### For Developers

**1. Update Imports**
```java
// Old
import com.microservices.demo.twitter.to.kafka.service.*;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;

// New
import com.microservices.demo.event.stream.service.*;
import com.microservices.demo.kafka.avro.model.SocialEventAvroModel;
```

**2. Update Dependency Injection**
```java
// Old
private final TwitterToKafkaServiceConfigData config;
private final KafkaProducer<Long, TwitterAvroModel> producer;

// New
private final EventStreamConfigData config;
private final KafkaProducer<Long, SocialEventAvroModel> producer;
```

## Rationale

This update reflects that the project:
1. **No longer uses Twitter API** - Removed in v2.0.0
2. **Generates generic social media events** - Not Twitter-specific
3. **Serves as a learning demo** - Focuses on Kafka patterns, not Twitter integration
4. **Is more maintainable** - Clear, accurate terminology

## Backward Compatibility

**Breaking Changes**:
- ⚠️ Module name changed (maven build)
- ⚠️ Package names changed (imports)
- ⚠️ Configuration keys changed (application.yml)
- ⚠️ Kafka topic name changed
- ⚠️ Avro model class name changed

**Compatible**:
- ✅ Avro schema fields unchanged (userId, id, text, createdAt)
- ✅ Kafka cluster configuration unchanged
- ✅ Docker Compose setup unchanged
- ✅ Spring Boot version unchanged
- ✅ All features and functionality preserved

## Benefits

1. **Clarity** - Name matches functionality
2. **Accuracy** - No misleading Twitter references
3. **Flexibility** - Easy to extend to other event types
4. **Maintainability** - Self-documenting code
5. **Professional** - Production-ready naming

---

**Version**: 3.1.0
**Date**: 2024
**Type**: Breaking Change (Major Version)
