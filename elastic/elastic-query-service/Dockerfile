# Multi-stage build for REST API Service
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy parent pom and dependencies
COPY pom.xml .
COPY app-config-data ./app-config-data
COPY common-config ./common-config
COPY elastic/elastic-model ./elastic/elastic-model
COPY elastic/elastic-config ./elastic/elastic-config
COPY elastic/elastic-query-client ./elastic/elastic-query-client

# Copy service
COPY elastic/elastic-query-service ./elastic/elastic-query-service

# Build
RUN mvn clean package -pl elastic/elastic-query-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy jar from build stage
COPY --from=build /app/elastic/elastic-query-service/target/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
