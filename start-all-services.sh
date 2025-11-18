#!/bin/bash

# Start All Services Script
# This script starts all microservices in the correct order

set -e

echo "======================================"
echo "  Starting All Microservices"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Step 1: Start Infrastructure (Kafka, Elasticsearch)
echo -e "${BLUE}Step 1: Starting Infrastructure (Kafka + Elasticsearch + Kibana)${NC}"
cd docker-compose
docker-compose -f kafka_cluster.yml up -d
cd ..

echo -e "${GREEN}Waiting for infrastructure to be ready (60 seconds)...${NC}"
sleep 60

# Step 2: Build All Services
echo -e "${BLUE}Step 2: Building all services with Maven${NC}"
mvn clean install -DskipTests

# Step 3: Start Event Stream Service (Producer)
echo -e "${BLUE}Step 3: Starting Event Stream Service (Producer - Port 8080)${NC}"
cd event-stream-service
mvn spring-boot:run > ../logs/event-stream-service.log 2>&1 &
EVENT_STREAM_PID=$!
echo "Event Stream Service PID: $EVENT_STREAM_PID"
cd ..
sleep 10

# Step 4: Start Kafka Consumer Service
echo -e "${BLUE}Step 4: Starting Kafka Consumer Service (Port 8081)${NC}"
cd kafka-consumer-service
mvn spring-boot:run > ../logs/kafka-consumer-service.log 2>&1 &
CONSUMER_PID=$!
echo "Kafka Consumer Service PID: $CONSUMER_PID"
cd ..
sleep 10

# Step 5: Start Kafka Streams Service
echo -e "${BLUE}Step 5: Starting Kafka Streams Service (Port 8082)${NC}"
cd kafka-streams-service
mvn spring-boot:run > ../logs/kafka-streams-service.log 2>&1 &
STREAMS_PID=$!
echo "Kafka Streams Service PID: $STREAMS_PID"
cd ..
sleep 10

# Step 6: Start Elasticsearch Indexing Service
echo -e "${BLUE}Step 6: Starting Elasticsearch Indexing Service (Port 8083)${NC}"
cd elasticsearch-service
mvn spring-boot:run > ../logs/elasticsearch-service.log 2>&1 &
ES_INDEXING_PID=$!
echo "Elasticsearch Indexing Service PID: $ES_INDEXING_PID"
cd ..
sleep 15

# Step 7: Start REST API Service
echo -e "${BLUE}Step 7: Starting REST API Service (Port 8084)${NC}"
cd elastic/elastic-query-service
mvn spring-boot:run > ../../logs/elastic-query-service.log 2>&1 &
API_PID=$!
echo "REST API Service PID: $API_PID"
cd ../..
sleep 10

echo ""
echo -e "${GREEN}======================================"
echo "  All Services Started Successfully!"
echo "======================================${NC}"
echo ""
echo "Service URLs:"
echo "  - Event Stream Service:       http://localhost:8080/actuator/health"
echo "  - Kafka Consumer Service:     http://localhost:8081/actuator/health"
echo "  - Kafka Streams Service:      http://localhost:8082/actuator/health"
echo "  - Elasticsearch Service:      http://localhost:8083/actuator/health"
echo "  - REST API Service:           http://localhost:8084/actuator/health"
echo "  - Swagger UI:                 http://localhost:8084/swagger-ui.html"
echo ""
echo "Infrastructure URLs:"
echo "  - Schema Registry:            http://localhost:8081"
echo "  - Elasticsearch:              http://localhost:9200"
echo "  - Kibana:                     http://localhost:5601"
echo ""
echo "Process IDs:"
echo "  - Event Stream:               $EVENT_STREAM_PID"
echo "  - Consumer:                   $CONSUMER_PID"
echo "  - Streams:                    $STREAMS_PID"
echo "  - ES Indexing:                $ES_INDEXING_PID"
echo "  - REST API:                   $API_PID"
echo ""
echo "Logs are available in the ./logs directory"
echo ""
echo "To stop all services, run: ./stop-all-services.sh"
echo ""
