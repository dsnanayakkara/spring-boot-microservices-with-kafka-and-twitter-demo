#!/bin/bash

# Stop All Services Script

set -e

echo "======================================"
echo "  Stopping All Microservices"
echo "======================================"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Stop Spring Boot applications (by port)
echo -e "${RED}Stopping Spring Boot services...${NC}"

# Function to kill process on port
kill_on_port() {
    PORT=$1
    SERVICE=$2
    PID=$(lsof -ti:$PORT)
    if [ ! -z "$PID" ]; then
        echo "Stopping $SERVICE on port $PORT (PID: $PID)"
        kill -9 $PID 2>/dev/null || true
    else
        echo "$SERVICE is not running on port $PORT"
    fi
}

# Stop all services
kill_on_port 8080 "Event Stream Service"
kill_on_port 8081 "Kafka Consumer Service"
kill_on_port 8082 "Kafka Streams Service"
kill_on_port 8083 "Elasticsearch Service"
kill_on_port 8084 "REST API Service"

# Stop Docker infrastructure
echo -e "${RED}Stopping Docker infrastructure...${NC}"
cd docker-compose
docker-compose -f kafka_cluster.yml down
cd ..

echo ""
echo -e "${GREEN}All services stopped successfully!${NC}"
echo ""
