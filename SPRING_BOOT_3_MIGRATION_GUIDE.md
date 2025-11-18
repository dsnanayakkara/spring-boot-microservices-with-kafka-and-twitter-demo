# Spring Boot 3.x Migration Guide

## Overview

This document outlines the migration from Spring Boot 2.7.18 to Spring Boot 3.2.x with Java 21.

## Current Stack
- **Java**: 11
- **Spring Boot**: 2.7.18
- **Spring Framework**: 5.3.x (implicit)
- **Spring Kafka**: 2.9.13
- **Jakarta EE**: N/A (using javax.*)

## Target Stack
- **Java**: 21 (LTS)
- **Spring Boot**: 3.2.x (latest stable)
- **Spring Framework**: 6.1.x (implicit)
- **Spring Kafka**: 3.1.x
- **Jakarta EE**: 10+

---

## Breaking Changes Identified

### 1. Jakarta EE Migration (javax → jakarta)

**Impact**: HIGH
**Files Affected**: 2

Spring Boot 3.0 requires Jakarta EE 9+ which renamed all `javax.*` packages to `jakarta.*`.

**Files to Update**:
1. `kafka/kafka-producer/src/main/java/.../AvroKafkaProducer.java`
   - Line 4: `import javax.annotation.PreDestroy;`

2. `event-stream-service/src/main/java/.../EnhancedMockStreamRunner.java`
   - Line 13: `import javax.annotation.PreDestroy;`

**Migration**:
```java
// Before
import javax.annotation.PreDestroy;

// After
import jakarta.annotation.PreDestroy;
```

**Required Dependency**:
```xml
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
</dependency>
```

---

### 2. ListenableFuture → CompletableFuture

**Impact**: HIGH
**Files Affected**: 1

Spring Framework 6.0 deprecated `ListenableFuture` in favor of `CompletableFuture`.

**File to Update**:
- `kafka/kafka-producer/src/main/java/.../AvroKafkaProducer.java`
  - Lines 11-12: `ListenableFuture` and `ListenableFutureCallback` imports
  - Lines 30-31: Return type changed
  - Lines 44-62: Callback pattern needs update

**Migration**:
```java
// Before
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

ListenableFuture<SendResult<Long, SocialEventAvroModel>> kafkaResultFuture =
    kafkaTemplate.send(topicName, key, message);

kafkaResultFuture.addCallback(new ListenableFutureCallback<>() {
    @Override
    public void onFailure(Throwable throwable) { ... }

    @Override
    public void onSuccess(SendResult<Long, SocialEventAvroModel> result) { ... }
});

// After
import java.util.concurrent.CompletableFuture;

CompletableFuture<SendResult<Long, SocialEventAvroModel>> kafkaResultFuture =
    kafkaTemplate.send(topicName, key, message);

kafkaResultFuture.whenComplete((result, throwable) -> {
    if (throwable != null) {
        // Handle failure
    } else {
        // Handle success
    }
});
```

---

### 3. WebClient exchange() Deprecation

**Impact**: MEDIUM
**Files Affected**: 1

Spring WebFlux 6.0 deprecated `.exchange()` in favor of `.exchangeToMono()` or `.retrieve()`.

**File to Update**:
- `kafka/kafka-admin/src/main/java/.../KafkaAdminClient.java`
  - Lines 94-99: WebClient usage

**Migration**:
```java
// Before
return webClient
    .method(HttpMethod.GET)
    .uri(kafkaConfigData.getSchemaRegistryUrl())
    .exchange()
    .map(ClientResponse::statusCode)
    .block();

// After
return webClient
    .method(HttpMethod.GET)
    .uri(kafkaConfigData.getSchemaRegistryUrl())
    .retrieve()
    .toBodilessEntity()
    .map(response -> response.getStatusCode())
    .block();
```

---

### 4. Dependency Version Updates

**Impact**: HIGH

#### Core Dependencies

| Dependency | Current | Target | Notes |
|------------|---------|--------|-------|
| Spring Boot | 2.7.18 | 3.2.5 | Latest stable |
| Java | 11 | 21 | LTS version |
| Spring Kafka | 2.9.13 | 3.1.4 | Auto-managed |
| Avro | 1.11.3 | 1.11.3 | Compatible |
| Confluent Kafka | 7.5.1 | 7.6.0 | Latest stable |
| Lombok | 1.18.30 | 1.18.32 | Latest |
| Maven Compiler | 3.11.0 | 3.13.0 | Latest |

#### Spring Boot Managed Dependencies
These will be automatically updated by Spring Boot 3.2.x parent POM:
- Spring Framework: 5.3.x → 6.1.x
- Spring Retry: 1.3.4 → 2.0.x
- Jackson: Auto-managed
- SLF4J: Auto-managed

---

### 5. Configuration Changes

**Impact**: LOW

#### application.yml Changes

Most Spring Boot 2.7 configurations are compatible, but verify:

1. **Actuator Endpoints** - No changes needed, configuration is compatible
2. **Kafka Configuration** - Compatible with Spring Kafka 3.x
3. **Logging** - Compatible

#### Properties Deprecations
None identified in current configuration.

---

### 6. Behavior Changes

#### Java 21 Features Available
- Virtual Threads (Project Loom)
- Pattern Matching for switch
- Record Patterns
- Sequenced Collections
- String Templates (Preview)

#### Spring Boot 3.x Features
- Native compilation support (GraalVM)
- Improved observability (Micrometer)
- Better Kubernetes support
- HTTP/3 support
- Virtual Threads support (with Java 21)

---

## Migration Strategy

### Phase 1: Preparation (Low Risk)
1. ✅ Review all breaking changes
2. ✅ Document migration plan
3. ✅ Backup current working branch

### Phase 2: Dependency Updates (Medium Risk)
1. Update Java version to 21
2. Update Spring Boot parent to 3.2.5
3. Update Confluent Kafka to 7.6.0
4. Update Lombok and other dependencies
5. Add Jakarta Annotation API dependency

### Phase 3: Code Changes (Medium Risk)
1. Replace `javax.annotation` with `jakarta.annotation`
2. Migrate `ListenableFuture` to `CompletableFuture`
3. Update WebClient `.exchange()` to `.retrieve()`

### Phase 4: Testing (High Importance)
1. Compile the project
2. Run tests (if any)
3. Start Kafka cluster
4. Run the application
5. Verify message generation
6. Check actuator endpoints
7. Monitor for errors

### Phase 5: Documentation
1. Update README with new versions
2. Update CHANGELOG
3. Update PROJECT_SUMMARY

---

## Compatibility Matrix

### Confirmed Compatible
- ✅ Avro 1.11.3 works with Spring Boot 3.x
- ✅ Confluent Platform 7.6.x compatible with Spring Kafka 3.x
- ✅ Docker Compose configuration unchanged
- ✅ Application configuration mostly unchanged

### Requires Updates
- ⚠️ Jakarta EE namespace migration required
- ⚠️ Future/Callback API changes required
- ⚠️ WebClient API update recommended

---

## Risk Assessment

| Risk Level | Category | Mitigation |
|------------|----------|------------|
| LOW | Configuration changes | Minimal config changes needed |
| MEDIUM | Dependency updates | Well-tested upgrade path |
| MEDIUM | Code changes | Limited changes, well-documented |
| LOW | Runtime behavior | Spring Boot 3.x is mature |

**Overall Risk**: MEDIUM-LOW

---

## Rollback Plan

If issues occur:
1. Git branch allows easy rollback: `git reset --hard HEAD~1`
2. Previous working code on main branch
3. Docker containers unchanged
4. No data migration required

---

## Timeline Estimate

- **Dependency Updates**: 15 minutes
- **Code Migration**: 30 minutes
- **Testing**: 30 minutes
- **Documentation**: 15 minutes

**Total**: ~1.5 hours

---

## Benefits of Migration

### Performance
- ✅ Java 21 performance improvements (~5-10% faster)
- ✅ Spring Framework 6 optimizations
- ✅ Better memory management

### Features
- ✅ Virtual Threads support (Project Loom)
- ✅ Native compilation ready (GraalVM)
- ✅ Better observability with Micrometer
- ✅ Modern Java features (pattern matching, records, etc.)

### Maintenance
- ✅ Long-term support (Java 21 LTS until 2029)
- ✅ Active development (Spring Boot 3.x)
- ✅ Security patches
- ✅ Better dependency management

---

## Post-Migration Verification Checklist

- [ ] Project compiles without errors
- [ ] Kafka cluster starts successfully
- [ ] Application starts without errors
- [ ] Messages are generated and sent to Kafka
- [ ] Actuator endpoints respond correctly
- [ ] Health check shows "UP" status
- [ ] No errors in application logs
- [ ] Kafka topics created successfully
- [ ] Schema Registry connection works
- [ ] Message rate statistics displayed

---

## References

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Jakarta EE 9 Migration](https://jakarta.ee/specifications/platform/9/)
- [Spring Framework 6.0 What's New](https://docs.spring.io/spring-framework/reference/6.0/whatsnew.html)
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)

---

**Status**: Ready to proceed with migration
**Date**: 2024
**Author**: Migration Analysis
