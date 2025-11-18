#!/bin/bash

# Deploy Demo Script - One-command deployment for demonstration purposes
# This script deploys the entire stack using Docker Compose

set -e

echo "================================================"
echo "Social Events Microservices - Demo Deployment"
echo "================================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    echo "Please install Docker first: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not installed${NC}"
    echo "Please install Docker Compose first: https://docs.docker.com/compose/install/"
    exit 1
fi

# Check if Maven is installed (needed for building)
if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}Warning: Maven is not installed${NC}"
    echo "Building will be done inside Docker containers (slower)"
    BUILD_WITH_MAVEN=false
else
    BUILD_WITH_MAVEN=true
fi

echo -e "${BLUE}Step 1: Checking Docker daemon...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker daemon is not running${NC}"
    echo "Please start Docker and try again"
    exit 1
fi
echo -e "${GREEN}✓ Docker is running${NC}"
echo ""

echo -e "${BLUE}Step 2: Building backend services...${NC}"
if [ "$BUILD_WITH_MAVEN" = true ]; then
    echo "Building with Maven (faster)..."
    mvn clean install -DskipTests
else
    echo "Building will be done by Docker Compose (slower)..."
fi
echo -e "${GREEN}✓ Backend services prepared${NC}"
echo ""

echo -e "${BLUE}Step 3: Building frontend dashboard...${NC}"
if command -v npm &> /dev/null; then
    cd dashboard-ui
    npm install
    cd ..
    echo -e "${GREEN}✓ Dashboard dependencies installed${NC}"
else
    echo -e "${YELLOW}Note: npm not found, building in Docker${NC}"
fi
echo ""

echo -e "${BLUE}Step 4: Starting infrastructure and services...${NC}"
echo "This may take 5-10 minutes on first run (downloading images)"
echo ""

cd docker-compose
docker-compose -f docker-compose-demo.yml up -d --build

echo ""
echo -e "${GREEN}✓ All services started!${NC}"
echo ""

echo -e "${BLUE}Step 5: Waiting for services to be healthy...${NC}"
echo "This may take 2-3 minutes..."
echo ""

# Function to check service health
check_health() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -sf "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $name is healthy${NC}"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 10
    done

    echo -e "${YELLOW}⚠ $name took longer than expected${NC}"
    return 1
}

# Wait for infrastructure
echo "Checking Kafka..."
check_health "http://localhost:8081" "Schema Registry"

echo "Checking Elasticsearch..."
check_health "http://localhost:9200" "Elasticsearch"

# Wait for services
echo ""
echo "Checking microservices..."
check_health "http://localhost:8080/actuator/health" "Event Stream Service"
check_health "http://localhost:8083/actuator/health" "Elasticsearch Service"
check_health "http://localhost:18081/actuator/health" "Kafka Consumer Service"
check_health "http://localhost:8082/actuator/health" "Kafka Streams Service"
check_health "http://localhost:8084/actuator/health" "REST API Service"
check_health "http://localhost:3000" "Dashboard UI"

echo ""
echo "================================================"
echo -e "${GREEN}🎉 Deployment Complete!${NC}"
echo "================================================"
echo ""
echo -e "${BLUE}Access your services:${NC}"
echo ""
echo -e "  📊 ${GREEN}Dashboard:${NC}        http://localhost:3000"
echo -e "  🔌 ${GREEN}REST API:${NC}         http://localhost:8084"
echo -e "  📖 ${GREEN}API Docs (Swagger):${NC} http://localhost:8084/swagger-ui.html"
echo -e "  📈 ${GREEN}Kibana:${NC}           http://localhost:5601"
echo -e "  🔍 ${GREEN}Elasticsearch:${NC}    http://localhost:9200"
echo ""
echo -e "${BLUE}Backend Services (Health Endpoints):${NC}"
echo -e "  🚀 ${GREEN}Event Stream:${NC}      http://localhost:8080/actuator/health"
echo -e "  📥 ${GREEN}Kafka Consumer:${NC}    http://localhost:18081/actuator/health"
echo -e "  🌊 ${GREEN}Kafka Streams:${NC}     http://localhost:8082/actuator/health"
echo -e "  💾 ${GREEN}Elasticsearch Svc:${NC} http://localhost:8083/actuator/health"
echo ""
echo -e "${BLUE}Service Health Checks:${NC}"
echo "  curl http://localhost:8080/actuator/health   # Event Stream Service"
echo "  curl http://localhost:8083/actuator/health   # Elasticsearch Service"
echo "  curl http://localhost:18081/actuator/health  # Kafka Consumer Service"
echo "  curl http://localhost:8082/actuator/health   # Kafka Streams Service"
echo "  curl http://localhost:8084/actuator/health   # REST API Service"
echo ""
echo -e "${BLUE}To view logs:${NC}"
echo "  docker-compose -f docker-compose/docker-compose-demo.yml logs -f [service-name]"
echo ""
echo -e "${BLUE}To stop all services:${NC}"
echo "  docker-compose -f docker-compose/docker-compose-demo.yml down"
echo ""
echo -e "${YELLOW}Note:${NC} It may take 1-2 minutes for events to appear in the dashboard"
echo "      as they are being generated, indexed, and queried."
echo ""
echo "================================================"
