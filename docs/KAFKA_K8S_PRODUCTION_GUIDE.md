# Kafka on Kubernetes Production Guide

## Table of Contents
1. [Production Readiness Checklist](#production-readiness-checklist)
2. [Kafka on Kubernetes Installation](#kafka-on-kubernetes-installation)
3. [Production Configuration](#production-configuration)
4. [Industry Standards & Best Practices](#industry-standards--best-practices)
5. [Security Hardening](#security-hardening)
6. [Monitoring & Observability](#monitoring--observability)
7. [Disaster Recovery](#disaster-recovery)

---

## Production Readiness Checklist

### Current State Analysis
Your project currently has:
- ✅ 3-broker Kafka cluster (dev setup)
- ✅ Schema Registry for Avro serialization
- ✅ Replication factor: 3
- ✅ min.insync.replicas: 2 (via transaction state log)
- ✅ Producer: acks=all, retries=5
- ✅ Health checks configured
- ❌ No authentication/authorization
- ❌ No encryption (plaintext listeners)
- ❌ No persistent storage configuration
- ❌ No resource limits/requests defined
- ❌ No monitoring stack (Prometheus/Grafana)

### Key Production Considerations

#### 1. **Infrastructure & Capacity Planning**
- **Kafka Brokers**: Minimum 3 brokers for HA (your dev setup is good)
- **Zookeeper**: 3 or 5 nodes (odd number for quorum)
- **Storage**:
  - Use persistent volumes with high IOPS (SSD/NVMe)
  - Size: 3-5x your expected daily throughput
  - Retention: Plan for 7-30 days minimum
- **Memory**:
  - Broker: 8-32 GB RAM (heap: 6-10 GB max)
  - Zookeeper: 2-4 GB RAM
- **CPU**:
  - Broker: 4-8 cores minimum
  - Scale based on partition count

#### 2. **Security** (CRITICAL - Currently Missing)
- **Authentication**: SASL/SCRAM or mTLS
- **Authorization**: Kafka ACLs
- **Encryption**:
  - TLS for client-broker communication
  - TLS for inter-broker communication
- **Network Policies**: K8s NetworkPolicies to restrict traffic
- **Secrets Management**: Use K8s Secrets or external vault (HashiCorp Vault)

#### 3. **Data Durability & Reliability**
- **Replication Factor**: 3 (minimum) ✅ Already configured
- **min.insync.replicas**: 2 ✅ Already configured
- **unclean.leader.election.enable**: false (prevent data loss)
- **Producer**: acks=all ✅ Already configured
- **Consumer**: enable.auto.commit=false (manual commit for exactly-once)

#### 4. **Performance Tuning**
- **Partitions**:
  - Formula: (Target throughput / Partition throughput) rounded up
  - Max partitions per broker: ~4000
  - Your setup: 3 partitions (may need more for scale)
- **Compression**: snappy ✅ Already configured
- **Batch size**: 16KB-32KB
- **linger.ms**: 5-10ms for throughput

#### 5. **Observability** (Currently Minimal)
- **Metrics**: JMX metrics → Prometheus
- **Logging**: Centralized (ELK/EFK stack or cloud logging)
- **Tracing**: Distributed tracing (Jaeger/Zipkin)
- **Alerting**: Key metrics to alert on (see monitoring section)

#### 6. **High Availability**
- **Multi-AZ deployment**: Spread brokers across availability zones
- **Pod Disruption Budgets**: Ensure minimum availability during maintenance
- **Anti-affinity rules**: Prevent broker pods on same node
- **Health checks**: Readiness and liveness probes

#### 7. **Scalability**
- **Horizontal Pod Autoscaler**: For application services
- **StatefulSet**: For Kafka and Zookeeper (ordered deployment)
- **Dynamic config**: Enable dynamic broker configuration

#### 8. **Application Configuration Updates**
Current Dev Issues to Fix:
```yaml
# event-stream-service/src/main/resources/application.yml
kafka-config:
  bootstrap-servers: localhost:19092, localhost:29092, localhost:39092  # ❌ Hardcoded localhost
```

Production Changes Needed:
- Externalize configuration (ConfigMaps/Secrets)
- Environment-specific profiles (dev, staging, prod)
- Service discovery (Kubernetes DNS)
- Connection pooling and timeout tuning
- Circuit breakers for resilience

---

## Kafka on Kubernetes Installation

### Option 1: Strimzi Operator (RECOMMENDED - Industry Standard)

Strimzi is the most popular and feature-rich Kafka operator for Kubernetes.

#### Prerequisites
```bash
# Kubernetes cluster 1.25+
# kubectl configured
# Helm 3.x installed
```

#### Installation Steps

**1. Install Strimzi Operator**
```bash
# Create namespace
kubectl create namespace kafka

# Install Strimzi using Helm
helm repo add strimzi https://strimzi.io/charts/
helm repo update

helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator \
  --namespace kafka \
  --version 0.38.0 \
  --set watchNamespaces="{kafka}"

# Verify installation
kubectl get pods -n kafka -w
```

**2. Deploy Kafka Cluster**
Create `kafka-cluster.yaml`:
```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: social-events-cluster
  namespace: kafka
spec:
  kafka:
    version: 3.6.0
    replicas: 3

    # Listeners for client connections
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: tls
        port: 9093
        type: internal
        tls: true
        authentication:
          type: scram-sha-512
      - name: external
        port: 9094
        type: loadbalancer
        tls: true
        authentication:
          type: scram-sha-512

    # Resource limits
    resources:
      requests:
        memory: 8Gi
        cpu: "2"
      limits:
        memory: 16Gi
        cpu: "4"

    # JVM settings
    jvmOptions:
      -Xms: 6144m
      -Xmx: 6144m

    # Storage configuration
    storage:
      type: jbod
      volumes:
      - id: 0
        type: persistent-claim
        size: 500Gi
        class: fast-ssd  # Use your storage class
        deleteClaim: false

    # Kafka configuration
    config:
      # Replication
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      default.replication.factor: 3
      min.insync.replicas: 2

      # Performance
      num.network.threads: 8
      num.io.threads: 16
      socket.send.buffer.bytes: 102400
      socket.receive.buffer.bytes: 102400
      socket.request.max.bytes: 104857600

      # Log settings
      log.retention.hours: 168  # 7 days
      log.segment.bytes: 1073741824  # 1 GB
      log.retention.check.interval.ms: 300000

      # Security
      auto.create.topics.enable: false
      unclean.leader.election.enable: false

      # Compression
      compression.type: producer

      # Group coordinator
      group.initial.rebalance.delay.ms: 3000

    # Metrics for Prometheus
    metricsConfig:
      type: jmxPrometheusExporter
      valueFrom:
        configMapKeyRef:
          name: kafka-metrics
          key: kafka-metrics-config.yml

    # Pod configuration
    template:
      pod:
        # Anti-affinity: don't schedule on same node
        affinity:
          podAntiAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
              - labelSelector:
                  matchExpressions:
                    - key: strimzi.io/name
                      operator: In
                      values:
                        - social-events-cluster-kafka
                topologyKey: kubernetes.io/hostname

        # Spread across availability zones
        topologySpreadConstraints:
          - maxSkew: 1
            topologyKey: topology.kubernetes.io/zone
            whenUnsatisfiable: DoNotSchedule
            labelSelector:
              matchLabels:
                strimzi.io/name: social-events-cluster-kafka

  zookeeper:
    replicas: 3

    resources:
      requests:
        memory: 2Gi
        cpu: "500m"
      limits:
        memory: 4Gi
        cpu: "1"

    storage:
      type: persistent-claim
      size: 100Gi
      class: fast-ssd
      deleteClaim: false

    metricsConfig:
      type: jmxPrometheusExporter
      valueFrom:
        configMapKeyRef:
          name: kafka-metrics
          key: zookeeper-metrics-config.yml

  entityOperator:
    topicOperator:
      resources:
        requests:
          memory: 512Mi
          cpu: "200m"
        limits:
          memory: 512Mi
          cpu: "500m"

    userOperator:
      resources:
        requests:
          memory: 512Mi
          cpu: "200m"
        limits:
          memory: 512Mi
          cpu: "500m"
```

**3. Deploy Schema Registry**
Create `schema-registry.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: schema-registry
  namespace: kafka
spec:
  replicas: 2
  selector:
    matchLabels:
      app: schema-registry
  template:
    metadata:
      labels:
        app: schema-registry
    spec:
      containers:
      - name: schema-registry
        image: confluentinc/cp-schema-registry:7.5.0
        ports:
        - containerPort: 8081
        env:
        - name: SCHEMA_REGISTRY_HOST_NAME
          value: schema-registry
        - name: SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS
          value: "social-events-cluster-kafka-bootstrap:9093"
        - name: SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL
          value: SASL_SSL
        - name: SCHEMA_REGISTRY_KAFKASTORE_SASL_MECHANISM
          value: SCRAM-SHA-512
        - name: SCHEMA_REGISTRY_KAFKASTORE_SASL_JAAS_CONFIG
          valueFrom:
            secretKeyRef:
              name: schema-registry-secret
              key: jaas.config
        - name: SCHEMA_REGISTRY_LISTENERS
          value: http://0.0.0.0:8081
        resources:
          requests:
            memory: 1Gi
            cpu: 500m
          limits:
            memory: 2Gi
            cpu: 1000m
        livenessProbe:
          httpGet:
            path: /
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 8081
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: schema-registry
  namespace: kafka
spec:
  selector:
    app: schema-registry
  ports:
  - protocol: TCP
    port: 8081
    targetPort: 8081
  type: ClusterIP
```

**4. Create Metrics ConfigMaps**
```bash
# Download metrics configs
kubectl create configmap kafka-metrics \
  --from-file=kafka-metrics-config.yml=https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/main/examples/metrics/kafka-metrics.yaml \
  --from-file=zookeeper-metrics-config.yml=https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/main/examples/metrics/zookeeper-metrics.yaml \
  -n kafka
```

**5. Deploy the Cluster**
```bash
kubectl apply -f kafka-cluster.yaml -n kafka

# Wait for cluster to be ready
kubectl wait kafka/social-events-cluster --for=condition=Ready --timeout=300s -n kafka

# Check status
kubectl get kafka -n kafka
kubectl get pods -n kafka
```

### Option 2: Confluent for Kubernetes (Enterprise)

```bash
# Install CFK operator
helm repo add confluentinc https://packages.confluent.io/helm
helm repo update

helm upgrade --install confluent-operator \
  confluentinc/confluent-for-kubernetes \
  --namespace confluent --create-namespace
```

### Option 3: Bitnami Kafka Helm Chart (Simple, but less production-ready)

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

helm install kafka bitnami/kafka \
  --namespace kafka \
  --create-namespace \
  --set replicaCount=3 \
  --set persistence.size=500Gi \
  --set auth.clientProtocol=sasl \
  --set auth.sasl.jaas.clientUsers={admin} \
  --set auth.sasl.jaas.clientPasswords={admin-password}
```

---

## Production Configuration

### 1. Kafka Topics (KafkaTopic CRD)

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: social-events
  namespace: kafka
  labels:
    strimzi.io/cluster: social-events-cluster
spec:
  partitions: 12  # Increased from 3 for better parallelism
  replicas: 3
  config:
    retention.ms: 604800000  # 7 days
    segment.ms: 3600000      # 1 hour
    compression.type: snappy
    min.insync.replicas: 2
    cleanup.policy: delete
    max.message.bytes: 1048576  # 1 MB
```

### 2. Kafka Users (SASL/SCRAM Authentication)

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaUser
metadata:
  name: event-stream-producer
  namespace: kafka
  labels:
    strimzi.io/cluster: social-events-cluster
spec:
  authentication:
    type: scram-sha-512
  authorization:
    type: simple
    acls:
      # Producer ACLs
      - resource:
          type: topic
          name: social-events
          patternType: literal
        operations:
          - Write
          - Describe
        host: "*"

      - resource:
          type: topic
          name: social-events
          patternType: literal
        operations:
          - Create
        host: "*"
---
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaUser
metadata:
  name: event-consumer
  namespace: kafka
  labels:
    strimzi.io/cluster: social-events-cluster
spec:
  authentication:
    type: scram-sha-512
  authorization:
    type: simple
    acls:
      # Consumer ACLs
      - resource:
          type: topic
          name: social-events
          patternType: literal
        operations:
          - Read
          - Describe
        host: "*"

      - resource:
          type: group
          name: social-events-consumer-group
          patternType: literal
        operations:
          - Read
        host: "*"
```

### 3. Application ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kafka-client-config
  namespace: default
data:
  application.yml: |
    kafka-config:
      bootstrap-servers: social-events-cluster-kafka-bootstrap.kafka.svc.cluster.local:9093
      schema-registry-url: http://schema-registry.kafka.svc.cluster.local:8081
      topic-name: social-events
      num-of-partitions: 12
      replication-factor: 3

      # Security
      security-protocol: SASL_SSL
      sasl-mechanism: SCRAM-SHA-512
      ssl-truststore-location: /etc/kafka/secrets/truststore.jks
      ssl-truststore-password: ${TRUSTSTORE_PASSWORD}

    kafka-producer-config:
      key-serializer-class: org.apache.kafka.common.serialization.LongSerializer
      value-serializer-class: io.confluent.kafka.serializers.KafkaAvroSerializer
      compression-type: snappy
      acks: all
      batch-size: 32768
      linger-ms: 10
      request-timeout-ms: 60000
      retry-count: 5
      max-in-flight-requests-per-connection: 5
      enable-idempotence: true  # Exactly-once semantics

    kafka-consumer-config:
      key-deserializer: org.apache.kafka.common.serialization.LongDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false  # Manual commit for exactly-once
      max-poll-records: 500
      session-timeout-ms: 30000
      heartbeat-interval-ms: 10000
      max-poll-interval-ms: 300000
```

### 4. Pod Disruption Budget

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: kafka-pdb
  namespace: kafka
spec:
  minAvailable: 2
  selector:
    matchLabels:
      strimzi.io/name: social-events-cluster-kafka
---
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: zookeeper-pdb
  namespace: kafka
spec:
  minAvailable: 2
  selector:
    matchLabels:
      strimzi.io/name: social-events-cluster-zookeeper
```

---

## Industry Standards & Best Practices

### 1. **Cluster Sizing Standards**

| Environment | Brokers | Zookeeper | Storage per Broker | RAM per Broker |
|-------------|---------|-----------|-------------------|----------------|
| Dev/Test    | 1-3     | 1         | 50-100 GB         | 4-8 GB         |
| Staging     | 3       | 3         | 200-500 GB        | 8-16 GB        |
| Production  | 3-5+    | 3-5       | 500 GB - 2 TB     | 16-32 GB       |
| Large Scale | 10-100+ | 5         | 1-10 TB           | 32-64 GB       |

### 2. **Topic Configuration Standards**

```yaml
# Standard topic configuration
partitions:
  - Formula: (Peak throughput MB/s) / (Single partition throughput ~10 MB/s)
  - Min: 3
  - Max per broker: 4000
  - Recommended: 12-24 for medium workloads

replication-factor: 3  # Always 3 in production

min.insync.replicas: 2  # Prevent data loss

retention:
  - Time-based: 7-30 days (168-720 hours)
  - Size-based: 70% of partition storage
  - Compacted topics: cleanup.policy=compact for event sourcing

segment.size: 1 GB  # Standard segment size
```

### 3. **Producer Best Practices**

```properties
# Reliability (at-least-once)
acks=all
enable.idempotence=true
max.in.flight.requests.per.connection=5
retries=Integer.MAX_VALUE

# Performance
compression.type=snappy  # or lz4, zstd for better compression
batch.size=32768  # 32 KB
linger.ms=10
buffer.memory=67108864  # 64 MB

# Timeouts
request.timeout.ms=60000
delivery.timeout.ms=120000
```

### 4. **Consumer Best Practices**

```properties
# Reliability
enable.auto.commit=false  # Manual commit
isolation.level=read_committed  # For transactional producers

# Performance
max.poll.records=500
fetch.min.bytes=1
fetch.max.wait.ms=500

# Timeouts
session.timeout.ms=30000
heartbeat.interval.ms=10000
max.poll.interval.ms=300000  # 5 minutes

# Offset management
auto.offset.reset=earliest  # Or 'latest' based on use case
```

### 5. **Monitoring KPIs (Key Performance Indicators)**

| Metric | Threshold | Action |
|--------|-----------|--------|
| Under-replicated partitions | > 0 | Critical alert |
| Offline partitions | > 0 | Critical alert |
| Active controller count | != 1 | Critical alert |
| Request handler idle % | < 20% | Scale up brokers |
| Network processor idle % | < 20% | Optimize network |
| Log flush latency | > 100ms | Check disk I/O |
| Replica lag | > 1000 msgs | Investigate consumers |
| Consumer lag | > 10000 msgs | Scale consumers |
| Produce request rate | Baseline | Capacity planning |
| Bytes in/out rate | 70% capacity | Scale up |

### 6. **Security Best Practices**

```yaml
# Security layers (Defense in depth)
1. Network: K8s NetworkPolicies, VPC isolation
2. Authentication: SASL/SCRAM-SHA-512 or mTLS
3. Authorization: Kafka ACLs (principle of least privilege)
4. Encryption: TLS 1.2+ for all communication
5. Secrets: External secret management (Vault, AWS Secrets Manager)
6. Audit: Enable audit logs
```

### 7. **Disaster Recovery Standards**

```yaml
Backup Strategy:
  - Automated snapshots: Daily
  - Cross-region replication: MirrorMaker 2
  - Retention: 30 days minimum

RTO (Recovery Time Objective):
  - Tier 1 (Critical): < 15 minutes
  - Tier 2 (Important): < 1 hour
  - Tier 3 (Standard): < 4 hours

RPO (Recovery Point Objective):
  - Tier 1: < 5 minutes data loss
  - Tier 2: < 30 minutes data loss
  - Tier 3: < 1 hour data loss
```

---

## Security Hardening

### 1. Enable TLS Encryption

Already configured in Strimzi manifest above. Extract certificates:

```bash
# Extract cluster CA certificate
kubectl get secret social-events-cluster-cluster-ca-cert \
  -n kafka \
  -o jsonpath='{.data.ca\.crt}' | base64 -d > ca.crt

# Import to Java truststore
keytool -import -trustcacerts -alias kafka-cluster \
  -file ca.crt \
  -keystore truststore.jks \
  -storepass changeit -noprompt
```

### 2. Create User Credentials Secret

```bash
# After creating KafkaUser, extract password
kubectl get secret event-stream-producer \
  -n kafka \
  -o jsonpath='{.data.password}' | base64 -d

# Create application secret
kubectl create secret generic kafka-credentials \
  --from-literal=username=event-stream-producer \
  --from-literal=password=<extracted-password> \
  --from-file=truststore.jks=truststore.jks \
  --from-literal=truststore-password=changeit \
  -n default
```

### 3. Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: kafka-network-policy
  namespace: kafka
spec:
  podSelector:
    matchLabels:
      strimzi.io/name: social-events-cluster-kafka
  policyTypes:
  - Ingress
  ingress:
  # Allow from application namespace
  - from:
    - namespaceSelector:
        matchLabels:
          name: default
    ports:
    - protocol: TCP
      port: 9093

  # Allow inter-broker communication
  - from:
    - podSelector:
        matchLabels:
          strimzi.io/name: social-events-cluster-kafka
    ports:
    - protocol: TCP
      port: 9091
    - protocol: TCP
      port: 9093

  # Allow from Zookeeper
  - from:
    - podSelector:
        matchLabels:
          strimzi.io/name: social-events-cluster-zookeeper
    ports:
    - protocol: TCP
      port: 2181
```

---

## Monitoring & Observability

### 1. Install Prometheus & Grafana

```bash
# Install Prometheus Operator
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false
```

### 2. Create ServiceMonitor for Kafka

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: kafka-metrics
  namespace: kafka
  labels:
    app: strimzi
spec:
  selector:
    matchLabels:
      strimzi.io/kind: Kafka
  endpoints:
  - port: tcp-prometheus
    interval: 30s
```

### 3. Import Grafana Dashboards

```bash
# Popular Kafka dashboards
# - Strimzi Kafka Dashboard: ID 11962
# - Kafka Exporter Dashboard: ID 7589
# - JMX Dashboard: ID 8563

# Access Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
# Login: admin / prom-operator
```

### 4. Critical Alerts

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: kafka-alerts
  namespace: kafka
spec:
  groups:
  - name: kafka
    interval: 30s
    rules:
    - alert: KafkaOfflinePartitions
      expr: kafka_controller_kafkacontroller_offlinepartitionscount > 0
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "Kafka has offline partitions"
        description: "{{ $value }} partitions are offline"

    - alert: KafkaUnderReplicatedPartitions
      expr: kafka_server_replicamanager_underreplicatedpartitions > 0
      for: 10m
      labels:
        severity: warning
      annotations:
        summary: "Kafka has under-replicated partitions"

    - alert: KafkaConsumerLag
      expr: kafka_consumergroup_lag > 10000
      for: 15m
      labels:
        severity: warning
      annotations:
        summary: "Consumer lag is high"
        description: "Consumer group {{ $labels.consumergroup }} lag: {{ $value }}"

    - alert: KafkaBrokerDown
      expr: kafka_brokers < 3
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "Kafka broker is down"
```

---

## Disaster Recovery

### 1. Backup Strategy

```yaml
# Use Velero for Kubernetes resource backup
apiVersion: velero.io/v1
kind: Schedule
metadata:
  name: kafka-backup
  namespace: velero
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  template:
    includedNamespaces:
    - kafka
    storageLocation: default
    volumeSnapshotLocations:
    - default
    ttl: 720h  # 30 days
```

### 2. MirrorMaker 2 for Cross-Region Replication

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaMirrorMaker2
metadata:
  name: kafka-mirror
  namespace: kafka
spec:
  version: 3.6.0
  replicas: 2
  connectCluster: "target-cluster"

  clusters:
  - alias: "source-cluster"
    bootstrapServers: social-events-cluster-kafka-bootstrap:9093

  - alias: "target-cluster"
    bootstrapServers: <DR-CLUSTER-ENDPOINT>:9093

  mirrors:
  - sourceCluster: "source-cluster"
    targetCluster: "target-cluster"
    sourceConnector:
      tasksMax: 4
      config:
        replication.factor: 3
        offset-syncs.topic.replication.factor: 3
        sync.topic.acls.enabled: "true"

    checkpointConnector:
      tasksMax: 4
      config:
        checkpoints.topic.replication.factor: 3

    topicsPattern: "social-events.*"
    groupsPattern: ".*"
```

### 3. Restore Procedure

```bash
# 1. Restore Kubernetes resources
velero restore create --from-backup kafka-backup-20250120

# 2. Verify cluster health
kubectl get kafka -n kafka
kubectl get pods -n kafka

# 3. Verify data
kubectl run kafka-consumer -ti \
  --image=quay.io/strimzi/kafka:latest-kafka-3.6.0 \
  --rm=true \
  --restart=Never \
  -- bin/kafka-console-consumer.sh \
  --bootstrap-server social-events-cluster-kafka-bootstrap:9092 \
  --topic social-events \
  --from-beginning \
  --max-messages 10
```

---

## Cost Optimization

### 1. Right-sizing Resources

```yaml
# Start conservative, then monitor and adjust
Initial sizing:
  Kafka broker:
    requests: { memory: 8Gi, cpu: 2 }
    limits: { memory: 16Gi, cpu: 4 }

  Zookeeper:
    requests: { memory: 2Gi, cpu: 500m }
    limits: { memory: 4Gi, cpu: 1 }

After 1 week of monitoring, adjust based on:
  - CPU usage < 50%: reduce limits
  - Memory usage: set requests = actual usage + 20% buffer
  - Disk IOPS: if < 30% utilized, consider cheaper storage class
```

### 2. Storage Tiering

```yaml
# Hot data: Fast SSD (last 7 days)
retention.ms: 604800000

# Warm data: Archive to object storage (S3/GCS)
# Use Kafka Tiered Storage (KIP-405) - Available in Kafka 3.6+
# Or implement custom archival to S3
```

---

## Migration from Docker Compose to Kubernetes

### Step-by-step Migration Plan

```bash
# 1. Deploy Kafka cluster in K8s (parallel to existing)
kubectl apply -f kafka-cluster.yaml

# 2. Set up MirrorMaker 2 to replicate from Docker Compose to K8s
# (Configure MirrorMaker to read from Docker Compose Kafka and write to K8s Kafka)

# 3. Migrate services one by one:
#    a. Deploy service to K8s
#    b. Configure to read from K8s Kafka
#    c. Run in parallel with Docker Compose version
#    d. Validate behavior
#    e. Switch traffic
#    f. Decommission Docker Compose version

# 4. Once all services migrated, decommission MirrorMaker

# 5. Decommission Docker Compose Kafka cluster
```

---

## Quick Start Commands

```bash
# 1. Install Strimzi
kubectl create namespace kafka
helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator -n kafka

# 2. Deploy Kafka cluster
kubectl apply -f kafka-cluster.yaml

# 3. Wait for ready
kubectl wait kafka/social-events-cluster --for=condition=Ready --timeout=300s -n kafka

# 4. Create topics
kubectl apply -f kafka-topics.yaml

# 5. Create users
kubectl apply -f kafka-users.yaml

# 6. Deploy Schema Registry
kubectl apply -f schema-registry.yaml

# 7. Get bootstrap servers
kubectl get kafka social-events-cluster -n kafka -o=jsonpath='{.status.listeners[?(@.name=="tls")].bootstrapServers}{"\n"}'

# 8. Deploy monitoring
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace

# 9. Test connection
kubectl run kafka-producer -ti --image=quay.io/strimzi/kafka:latest-kafka-3.6.0 --rm=true --restart=Never -- bin/kafka-console-producer.sh --bootstrap-server social-events-cluster-kafka-bootstrap:9092 --topic social-events
```

---

## References

- **Strimzi Documentation**: https://strimzi.io/docs/operators/latest/overview.html
- **Kafka on Kubernetes Best Practices**: https://www.confluent.io/blog/kafka-kubernetes-deployment-best-practices/
- **Apache Kafka Production Checklist**: https://docs.confluent.io/platform/current/kafka/deployment.html
- **Kafka Sizing Calculator**: https://eventsizer.io/
- **Monitoring Kafka**: https://kafka.apache.org/documentation/#monitoring

---

## Next Steps for This Project

1. **Create Kubernetes manifests** for all microservices
2. **Implement SASL/SCRAM authentication** in application configs
3. **Set up CI/CD pipeline** (GitHub Actions) for automated deployment
4. **Create Helm charts** for easier deployment
5. **Implement distributed tracing** (Jaeger/Zipkin)
6. **Add load testing** (K6, JMeter) to validate performance
7. **Document runbooks** for common operational tasks
8. **Set up chaos engineering** (Chaos Mesh) to test resilience

