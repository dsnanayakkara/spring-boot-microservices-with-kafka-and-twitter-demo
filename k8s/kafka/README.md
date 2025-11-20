# Kafka on Kubernetes Deployment

This directory contains Kubernetes manifests for deploying Apache Kafka using the Strimzi operator.

## Quick Start

### 1. Install Strimzi Operator

```bash
chmod +x 00-install-strimzi.sh
./00-install-strimzi.sh
```

Or manually:
```bash
kubectl create namespace kafka
helm repo add strimzi https://strimzi.io/charts/
helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator -n kafka
```

### 2. Deploy Kafka Cluster

```bash
# Deploy the Kafka cluster (this will take 3-5 minutes)
kubectl apply -f 01-kafka-cluster.yaml

# Wait for cluster to be ready
kubectl wait kafka/social-events-cluster --for=condition=Ready --timeout=600s -n kafka

# Check status
kubectl get kafka -n kafka
kubectl get pods -n kafka
```

### 3. Create Topics

```bash
kubectl apply -f 02-kafka-topics.yaml

# Verify topics
kubectl get kafkatopic -n kafka
```

### 4. Create Users (with SASL authentication)

```bash
kubectl apply -f 03-kafka-users.yaml

# Verify users
kubectl get kafkauser -n kafka
```

### 5. Deploy Schema Registry

```bash
kubectl apply -f 04-schema-registry.yaml

# Wait for Schema Registry to be ready
kubectl wait --for=condition=available deployment/schema-registry -n kafka --timeout=300s

# Check status
kubectl get pods -n kafka -l app=schema-registry
```

### 6. Apply High Availability Configurations

```bash
# Pod Disruption Budgets
kubectl apply -f 05-pod-disruption-budgets.yaml

# Network Policies (optional - for enhanced security)
kubectl apply -f 06-network-policies.yaml
```

## Access Kafka

### Internal Access (from within Kubernetes)

**Bootstrap servers:**
- Plaintext (dev only): `social-events-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092`
- TLS with SASL: `social-events-cluster-kafka-bootstrap.kafka.svc.cluster.local:9093`

**Schema Registry:**
- `http://schema-registry.kafka.svc.cluster.local:8081`

### External Access (from outside Kubernetes)

```bash
# Port-forward to access locally
kubectl port-forward svc/social-events-cluster-kafka-bootstrap 9092:9092 -n kafka

# In another terminal
kafka-console-producer --bootstrap-server localhost:9092 --topic social-events
```

## Retrieve User Credentials

```bash
# Get password for a user (e.g., event-stream-producer)
kubectl get secret event-stream-producer -n kafka -o jsonpath='{.data.password}' | base64 -d
echo

# Get SASL JAAS config
kubectl get secret event-stream-producer -n kafka -o jsonpath='{.data.sasl\.jaas\.config}' | base64 -d
echo
```

## Extract TLS Certificates

```bash
# Extract cluster CA certificate
kubectl get secret social-events-cluster-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.crt}' | base64 -d > ca.crt

# Extract PKCS12 truststore
kubectl get secret social-events-cluster-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.p12}' | base64 -d > truststore.p12

# Get truststore password
kubectl get secret social-events-cluster-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.password}' | base64 -d
echo
```

## Testing the Deployment

### Test Producer

```bash
# Create a test producer
kubectl run kafka-producer -ti \
  --image=quay.io/strimzi/kafka:latest-kafka-3.6.0 \
  --rm=true \
  --restart=Never \
  -n kafka \
  -- bin/kafka-console-producer.sh \
  --bootstrap-server social-events-cluster-kafka-bootstrap:9092 \
  --topic social-events

# Type some messages and press Ctrl+C when done
```

### Test Consumer

```bash
# Create a test consumer
kubectl run kafka-consumer -ti \
  --image=quay.io/strimzi/kafka:latest-kafka-3.6.0 \
  --rm=true \
  --restart=Never \
  -n kafka \
  -- bin/kafka-console-consumer.sh \
  --bootstrap-server social-events-cluster-kafka-bootstrap:9092 \
  --topic social-events \
  --from-beginning
```

### Test Schema Registry

```bash
# Port-forward Schema Registry
kubectl port-forward svc/schema-registry 8081:8081 -n kafka

# In another terminal, test the connection
curl http://localhost:8081/subjects
```

## Monitoring

### View Kafka Logs

```bash
# Kafka broker logs
kubectl logs -f social-events-cluster-kafka-0 -n kafka

# Zookeeper logs
kubectl logs -f social-events-cluster-zookeeper-0 -n kafka

# Schema Registry logs
kubectl logs -f deployment/schema-registry -n kafka
```

### Check Metrics

```bash
# Port-forward to access JMX metrics
kubectl port-forward social-events-cluster-kafka-0 9404:9404 -n kafka

# In another terminal
curl http://localhost:9404/metrics
```

## Scaling

### Scale Kafka Brokers

```bash
# Edit the Kafka CR
kubectl edit kafka social-events-cluster -n kafka

# Change spec.kafka.replicas to desired number (e.g., 5)
# Save and exit - Strimzi will handle the scaling
```

### Scale Schema Registry

```bash
kubectl scale deployment schema-registry --replicas=3 -n kafka
```

## Maintenance

### Rolling Update

Strimzi handles rolling updates automatically when you change configuration:

```bash
# Edit Kafka configuration
kubectl edit kafka social-events-cluster -n kafka

# Strimzi will perform a rolling update of brokers
kubectl get pods -n kafka -w
```

### Backup

```bash
# Use Velero or similar tool for backup
velero backup create kafka-backup --include-namespaces kafka
```

### Disaster Recovery

See the main production guide: `../../docs/KAFKA_K8S_PRODUCTION_GUIDE.md`

## Troubleshooting

### Check Cluster Status

```bash
kubectl get kafka social-events-cluster -n kafka -o yaml
kubectl describe kafka social-events-cluster -n kafka
```

### Check Events

```bash
kubectl get events -n kafka --sort-by='.lastTimestamp'
```

### Common Issues

**1. Kafka pods in CrashLoopBackOff**
```bash
# Check logs
kubectl logs social-events-cluster-kafka-0 -n kafka

# Common causes:
# - Insufficient resources
# - Storage issues
# - Network connectivity problems
```

**2. Topics not created**
```bash
# Check topic operator logs
kubectl logs deployment/social-events-cluster-entity-operator -c topic-operator -n kafka

# Verify topic CR
kubectl describe kafkatopic social-events -n kafka
```

**3. Authentication failures**
```bash
# Verify user exists
kubectl get kafkauser -n kafka

# Check user status
kubectl describe kafkauser event-stream-producer -n kafka

# Verify secret was created
kubectl get secret event-stream-producer -n kafka
```

## Configuration Customization

### Storage Class

Update `01-kafka-cluster.yaml` to use your storage class:

```yaml
storage:
  type: persistent-claim
  size: 500Gi
  class: fast-ssd  # Change this to your storage class
```

### Resource Limits

Adjust based on your workload in `01-kafka-cluster.yaml`:

```yaml
resources:
  requests:
    memory: 8Gi   # Adjust based on load
    cpu: "2"
  limits:
    memory: 16Gi
    cpu: "4"
```

### Partition Count

Adjust topic partitions in `02-kafka-topics.yaml`:

```yaml
spec:
  partitions: 12  # Increase for higher throughput
```

## Security Considerations

For production deployments:

1. **Always use TLS** - Remove plaintext listener (port 9092)
2. **Enable SASL authentication** - Already configured for port 9093
3. **Apply Network Policies** - Run `06-network-policies.yaml`
4. **Use external secrets management** - Consider using HashiCorp Vault or AWS Secrets Manager
5. **Enable audit logging** - Configure Kafka audit logs
6. **Regular security updates** - Keep Strimzi and Kafka versions updated

See the full security guide: `../../docs/KAFKA_K8S_PRODUCTION_GUIDE.md#security-hardening`

## Next Steps

1. Deploy your application microservices with updated Kafka configuration
2. Set up monitoring with Prometheus and Grafana
3. Configure alerting for critical metrics
4. Implement backup and disaster recovery procedures
5. Perform load testing to validate performance

See the complete production setup guide: `../../docs/KAFKA_K8S_PRODUCTION_GUIDE.md`
