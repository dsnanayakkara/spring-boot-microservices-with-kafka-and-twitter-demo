# Multi-stage build for Kafka Consumer Service
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy parent pom and dependencies
COPY pom.xml .
COPY app-config-data ./app-config-data
COPY common-config ./common-config
COPY common-security ./common-security
COPY kafka/pom.xml ./kafka/pom.xml
COPY kafka/kafka-model ./kafka/kafka-model
COPY kafka/kafka-admin ./kafka/kafka-admin
COPY kafka/kafka-producer ./kafka/kafka-producer
COPY kafka/kafka-consumer ./kafka/kafka-consumer
COPY elastic/pom.xml ./elastic/pom.xml
COPY elastic/elastic-model ./elastic/elastic-model
COPY elastic/elastic-config ./elastic/elastic-config
COPY elastic/elastic-index-client ./elastic/elastic-index-client
COPY elastic/elastic-query-client ./elastic/elastic-query-client
COPY elastic/elastic-query-service ./elastic/elastic-query-service
COPY event-stream-service ./event-stream-service
COPY kafka-streams-service ./kafka-streams-service
COPY elasticsearch-service ./elasticsearch-service

# Copy service
COPY kafka-consumer-service ./kafka-consumer-service

# Build
RUN mvn clean package -pl kafka-consumer-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy jar from build stage
COPY --from=build /app/kafka-consumer-service/target/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
