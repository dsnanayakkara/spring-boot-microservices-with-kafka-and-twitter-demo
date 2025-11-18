# Multi-stage build for Event Stream Service
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy parent pom and dependencies
COPY pom.xml .
COPY app-config-data ./app-config-data
COPY common-config ./common-config
COPY kafka/kafka-model ./kafka/kafka-model
COPY kafka/kafka-admin ./kafka/kafka-admin
COPY kafka/kafka-producer ./kafka/kafka-producer

# Copy service
COPY event-stream-service ./event-stream-service

# Build
RUN mvn clean package -pl event-stream-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy jar from build stage
COPY --from=build /app/event-stream-service/target/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
