# Quick Start Guide

Get up and running with the Kafka Microservices demo in 5 minutes!

## Prerequisites Checklist

- [ ] Java 11 or higher installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] Docker installed and running (`docker --version`)
- [ ] Docker Compose installed (`docker-compose --version`)
- [ ] At least 4GB RAM allocated to Docker

## 3-Step Setup

### 1️⃣ Start Kafka Infrastructure (2 minutes)

```bash
cd docker-compose
docker-compose -f kafka_cluster.yml up -d
```

**Wait for services to be healthy:**
```bash
# Check all services are running
docker-compose -f kafka_cluster.yml ps

# Should see all services "Up" and "healthy"
```

### 2️⃣ Build the Application (1-2 minutes)

```bash
cd ..
mvn clean install -DskipTests
```

### 3️⃣ Run the Application (30 seconds)

```bash
cd twitter-to-kafka-service
mvn spring-boot:run
```

**That's it! The application is now running and generating messages.**

## Verify It's Working

### Check Application Health

```bash
curl http://localhost:8080/actuator/health
```

Expected output:
```json
{
  "status": "UP",
  "components": {
    "kafka": {
      "status": "UP"
    }
  }
}
```

### Monitor the Logs

You should see messages like:
```
2024-XX-XX XX:XX:XX - Starting enhanced mock data stream for keywords: [Java, Microservices, ...]
2024-XX-XX XX:XX:XX - Generated message 1234567: Just deployed a new microservice...
2024-XX-XX XX:XX:XX - 📊 Messages generated so far: 150 | Average rate: 60.00 msgs/min
```

### View Metrics

```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# Kafka-specific metrics
curl http://localhost:8080/actuator/metrics/kafka.producer.record-send-total
```

## Stop Everything

### Stop Application
Press `Ctrl+C` in the terminal where it's running

### Stop Kafka Cluster
```bash
cd docker-compose
docker-compose -f kafka_cluster.yml down
```

## Troubleshooting

### "Cannot connect to Kafka"
- Ensure Docker is running
- Check Kafka containers: `docker ps`
- Restart Kafka cluster: `docker-compose -f kafka_cluster.yml restart`

### "Port 8080 already in use"
Change the port in `application.yml`:
```yaml
server:
  port: 8081  # or any available port
```

### Build Fails
- Ensure Maven can access the internet
- Try: `mvn clean install -U` (force update dependencies)

### Out of Memory Errors
- Increase Docker RAM allocation to at least 4GB
- Check: Docker Desktop → Settings → Resources → Memory

## What's Happening Behind the Scenes?

1. **Kafka Cluster**: 3 brokers running for high availability
2. **Schema Registry**: Managing Avro schemas at http://localhost:8081
3. **Message Generator**: Creating realistic social media messages every 1 second
4. **Kafka Producer**: Sending Avro-serialized messages to the `twitter-topic` topic
5. **Monitoring**: Health checks and metrics available via Spring Boot Actuator

## Next Steps

- [ ] Read the full [README.md](README.md) for detailed information
- [ ] Check out [CHANGELOG.md](CHANGELOG.md) to see what changed
- [ ] Modify `application.yml` to customize message generation
- [ ] Build a Kafka Consumer to process messages
- [ ] Explore the Actuator endpoints
- [ ] Add Kafka Streams processing
- [ ] Integrate with Elasticsearch for message storage

## Quick Configuration Tips

### Generate Messages Faster
Edit `twitter-to-kafka-service/src/main/resources/application.yml`:
```yaml
twitter-to-kafka-service:
  mock-sleep-ms: 500  # Generate every 500ms (2 per second)
```

### Add Custom Keywords
```yaml
twitter-to-kafka-service:
  twitter-keywords:
    - MyKeyword1
    - MyKeyword2
    - AnotherTopic
```

### Enable Debug Logging
```yaml
logging:
  level:
    com.microservices.demo: DEBUG
```

## Useful Commands

```bash
# View Kafka topics
docker exec kafka-broker-1 kafka-topics --list --bootstrap-server localhost:9092

# Check topic details
docker exec kafka-broker-1 kafka-topics --describe --topic twitter-topic --bootstrap-server localhost:9092

# Consume messages from topic
docker exec kafka-broker-1 kafka-console-consumer --topic twitter-topic --from-beginning --bootstrap-server localhost:9092

# View Schema Registry schemas
curl http://localhost:8081/subjects

# Check application logs in real-time
cd twitter-to-kafka-service && mvn spring-boot:run | grep "Generated message"
```

## Getting Help

- Check the [README.md](README.md) for comprehensive documentation
- Review [CHANGELOG.md](CHANGELOG.md) for recent changes
- Examine logs for error messages
- Verify all Docker containers are healthy

---

**Happy Learning! 🚀**
