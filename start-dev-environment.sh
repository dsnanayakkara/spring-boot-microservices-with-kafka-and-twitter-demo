#!/bin/bash

echo "=========================================="
echo "Starting Kafka Microservices Dev Environment"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo ""
echo -e "${YELLOW}Step 1: Starting Kafka Cluster...${NC}"
cd docker-compose
docker-compose -f kafka_cluster.yml up -d

echo ""
echo -e "${YELLOW}Waiting for services to be healthy...${NC}"
sleep 30

echo ""
echo -e "${YELLOW}Step 2: Checking service status...${NC}"
docker-compose -f kafka_cluster.yml ps

echo ""
echo -e "${GREEN}Kafka Cluster started successfully!${NC}"
echo ""
echo "Services running:"
echo "  - Zookeeper: localhost:2181"
echo "  - Kafka Broker 1: localhost:19092"
echo "  - Kafka Broker 2: localhost:29092"
echo "  - Kafka Broker 3: localhost:39092"
echo "  - Schema Registry: http://localhost:8081"
echo ""
echo -e "${YELLOW}Step 3: Building the application...${NC}"
cd ..
mvn clean install -DskipTests

echo ""
echo -e "${GREEN}Build complete!${NC}"
echo ""
echo "To start the application, run:"
echo "  cd event-stream-service && mvn spring-boot:run"
echo ""
echo "Once running, check health at:"
echo "  http://localhost:8080/actuator/health"
echo ""
