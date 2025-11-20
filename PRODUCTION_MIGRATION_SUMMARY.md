# Production Migration Summary: Kafka on Kubernetes

## Overview

This document provides a comprehensive guide for migrating your Spring Boot microservices demo from a development Docker Compose setup to a production-ready Kafka deployment on Kubernetes.

## What's Been Created

### 1. Documentation
- **`docs/KAFKA_K8S_PRODUCTION_GUIDE.md`**: Comprehensive 8-section production guide covering:
  - Production readiness checklist (current state analysis)
  - Kafka on Kubernetes installation (3 options: Strimzi, Confluent, Bitnami)
  - Production configuration (topics, users, security)
  - Industry standards & best practices
  - Security hardening
  - Monitoring & observability
  - Disaster recovery
  - Cost optimization

### 2. Kubernetes Manifests (`k8s/kafka/`)
- **`00-install-strimzi.sh`**: Automated installation script for Strimzi operator
- **`01-kafka-cluster.yaml`**: Production-ready 3-broker Kafka cluster with:
  - High availability configuration
  - TLS encryption
  - SASL/SCRAM authentication
  - Persistent storage
  - Anti-affinity rules
  - Resource limits
  - Prometheus metrics export

- **`02-kafka-topics.yaml`**: KafkaTopic CRDs for:
  - `social-events` (main topic)
  - `social-events-filtered`
  - `social-events-word-count`
  - Kafka Streams changelog topics

- **`03-kafka-users.yaml`**: KafkaUser CRDs with ACLs for:
  - `event-stream-producer`
  - `event-consumer`
  - `kafka-streams-user`
  - `elasticsearch-indexer`
  - `schema-registry-user`
  - `kafka-admin`

- **`04-schema-registry.yaml`**: Schema Registry deployment with:
  - 2 replicas for HA
  - TLS connection to Kafka
  - SASL authentication
  - Health checks

- **`05-pod-disruption-budgets.yaml`**: PDBs for Kafka, Zookeeper, and Schema Registry

- **`06-network-policies.yaml`**: Network policies for security isolation

- **`README.md`**: Step-by-step deployment guide with troubleshooting

## Key Production Considerations Addressed

### Current State (Dev Setup)
✅ 3-broker Kafka cluster
✅ Schema Registry for Avro serialization
✅ Replication factor: 3
✅ min.insync.replicas: 2
✅ Producer: acks=all, retries=5
✅ Health checks configured

### Gaps Fixed in Production Setup
❌ → ✅ Authentication/Authorization (SASL/SCRAM + Kafka ACLs)
❌ → ✅ Encryption (TLS for all communication)
❌ → ✅ Persistent storage (PVCs with configurable storage class)
❌ → ✅ Resource limits/requests (CPU, memory)
❌ → ✅ Monitoring stack (Prometheus metrics export)
❌ → ✅ High availability (pod anti-affinity, PDBs)
❌ → ✅ Network security (NetworkPolicies)

## Industry Standards Implemented

### 1. Cluster Sizing
- **Brokers**: 3 (minimum for production)
- **Zookeeper**: 3 (odd number for quorum)
- **Storage**: 500 GB per broker (configurable)
- **Memory**: 8-16 GB per broker
- **CPU**: 2-4 cores per broker

### 2. Kafka Configuration
```yaml
replication.factor: 3
min.insync.replicas: 2
acks: all
enable.idempotence: true
unclean.leader.election.enable: false
auto.create.topics.enable: false
```

### 3. Topic Configuration
- **Partitions**: 12 (increased from 3 for better parallelism)
- **Retention**: 7 days (configurable)
- **Compression**: snappy
- **Segment size**: 1 GB

### 4. Security
- **Authentication**: SASL/SCRAM-SHA-512
- **Authorization**: Kafka ACLs (principle of least privilege)
- **Encryption**: TLS 1.2+ for all communication
- **Network**: Kubernetes NetworkPolicies

### 5. Monitoring
- **Metrics**: JMX → Prometheus
- **Key metrics**: Under-replicated partitions, consumer lag, offline partitions
- **Alerting**: PrometheusRule for critical alerts
- **Dashboards**: Grafana dashboards (Strimzi, Kafka Exporter)

## Installation Options Compared

| Feature | Strimzi (RECOMMENDED) | Confluent for K8s | Bitnami Helm |
|---------|---------------------|-------------------|--------------|
| Cost | Free (Apache 2.0) | Enterprise (paid) | Free |
| CRDs | ✅ Kafka, KafkaTopic, KafkaUser | ✅ Advanced CRDs | ❌ Basic |
| Features | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| Community | Very active | Active (commercial) | Active |
| Production-ready | ✅ | ✅ | ⚠️ Limited |
| Ease of use | High | Medium | Very high |
| Monitoring | Built-in | Built-in | Manual setup |
| Best for | Production | Enterprise | Dev/Test |

**Recommendation**: Use Strimzi for production. It's the industry standard, actively maintained by Red Hat/IBM, and provides the best balance of features and ease of use.

## Deployment Steps

### Quick Start (5 commands)

```bash
# 1. Install Strimzi operator
cd k8s/kafka
chmod +x 00-install-strimzi.sh
./00-install-strimzi.sh

# 2. Deploy Kafka cluster
kubectl apply -f 01-kafka-cluster.yaml

# 3. Wait for cluster to be ready (3-5 minutes)
kubectl wait kafka/social-events-cluster --for=condition=Ready --timeout=600s -n kafka

# 4. Create topics and users
kubectl apply -f 02-kafka-topics.yaml
kubectl apply -f 03-kafka-users.yaml

# 5. Deploy Schema Registry
kubectl apply -f 04-schema-registry.yaml
```

### Full Production Deployment

See `k8s/kafka/README.md` for detailed step-by-step instructions.

## Application Migration Checklist

### 1. Update Kafka Configuration

Current (Docker Compose):
```yaml
kafka-config:
  bootstrap-servers: localhost:19092, localhost:29092, localhost:39092
  schema-registry-url: http://localhost:8081
```

Production (Kubernetes):
```yaml
kafka-config:
  bootstrap-servers: social-events-cluster-kafka-bootstrap.kafka.svc.cluster.local:9093
  schema-registry-url: http://schema-registry.kafka.svc.cluster.local:8081
  security-protocol: SASL_SSL
  sasl-mechanism: SCRAM-SHA-512
```

### 2. Add Security Configuration

```yaml
kafka-producer-config:
  # Existing config...
  enable-idempotence: true
  max-in-flight-requests-per-connection: 5

# New security config
spring:
  kafka:
    properties:
      sasl.jaas.config: ${KAFKA_SASL_JAAS_CONFIG}
      ssl.truststore.location: /etc/kafka/secrets/truststore.p12
      ssl.truststore.password: ${KAFKA_TRUSTSTORE_PASSWORD}
      ssl.truststore.type: PKCS12
```

### 3. Create Kubernetes Secrets

```bash
# Get user credentials
KAFKA_PASSWORD=$(kubectl get secret event-stream-producer -n kafka -o jsonpath='{.data.password}' | base64 -d)

# Create application secret
kubectl create secret generic kafka-credentials \
  --from-literal=username=event-stream-producer \
  --from-literal=password=$KAFKA_PASSWORD \
  --from-literal=jaas-config="org.apache.kafka.common.security.scram.ScramLoginModule required username=\"event-stream-producer\" password=\"$KAFKA_PASSWORD\";" \
  -n default
```

### 4. Update Application Deployment

Add volume mounts for TLS certificates:
```yaml
volumeMounts:
  - name: kafka-truststore
    mountPath: /etc/kafka/secrets
    readOnly: true

volumes:
  - name: kafka-truststore
    secret:
      secretName: social-events-cluster-cluster-ca-cert
      items:
      - key: ca.p12
        path: truststore.p12
```

### 5. Environment Variables

```yaml
env:
  - name: KAFKA_SASL_JAAS_CONFIG
    valueFrom:
      secretKeyRef:
        name: kafka-credentials
        key: jaas-config

  - name: KAFKA_TRUSTSTORE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: social-events-cluster-cluster-ca-cert
        key: ca.password
```

## Migration Strategy

### Option 1: Blue-Green Deployment (Recommended)

1. Deploy Kafka cluster in K8s (parallel to existing Docker Compose)
2. Set up MirrorMaker 2 to replicate from Docker Compose to K8s
3. Deploy one microservice to K8s, validate behavior
4. Migrate remaining services one by one
5. Switch DNS/traffic to K8s services
6. Decommission Docker Compose setup

**Downtime**: Zero
**Risk**: Low
**Duration**: 1-2 weeks

### Option 2: Direct Migration

1. Schedule maintenance window
2. Stop all services
3. Deploy Kafka cluster in K8s
4. Restore data from backup (if needed)
5. Deploy all services to K8s
6. Validate and go live

**Downtime**: 2-4 hours
**Risk**: Medium
**Duration**: 1-2 days

## Post-Migration Tasks

### 1. Set Up Monitoring

```bash
# Install Prometheus and Grafana
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace

# Import Kafka dashboards (IDs: 11962, 7589, 8563)
```

### 2. Configure Alerting

Apply the PrometheusRule for Kafka alerts:
- Offline partitions
- Under-replicated partitions
- Consumer lag
- Broker down

### 3. Implement Backup Strategy

```bash
# Install Velero for Kubernetes resource backup
velero install --provider <your-cloud-provider>

# Create backup schedule
velero schedule create kafka-daily \
  --schedule="0 2 * * *" \
  --include-namespaces kafka \
  --ttl 720h
```

### 4. Load Testing

```bash
# Use kafka-producer-perf-test to validate performance
kubectl run kafka-perf-test -ti \
  --image=quay.io/strimzi/kafka:latest-kafka-3.6.0 \
  --rm=true \
  --restart=Never \
  -n kafka \
  -- bin/kafka-producer-perf-test.sh \
  --topic social-events \
  --num-records 1000000 \
  --record-size 1024 \
  --throughput 10000 \
  --producer-props bootstrap.servers=social-events-cluster-kafka-bootstrap:9092
```

### 5. Disaster Recovery Testing

1. Test backup/restore procedures
2. Simulate broker failure
3. Test consumer lag recovery
4. Validate data integrity

## Cost Optimization Tips

1. **Right-size resources**: Start conservative, monitor, and adjust
2. **Use appropriate storage class**: Don't use premium SSD if standard SSD suffices
3. **Implement data retention policies**: 7-30 days retention based on compliance needs
4. **Consider tiered storage**: Kafka 3.6+ supports tiered storage to S3/GCS
5. **Monitor idle resources**: Scale down during off-peak hours if possible

## Monitoring KPIs

| Metric | Threshold | Priority |
|--------|-----------|----------|
| Under-replicated partitions | > 0 | P0 (Critical) |
| Offline partitions | > 0 | P0 (Critical) |
| Active controller count | != 1 | P0 (Critical) |
| Consumer lag | > 10000 msgs | P1 (High) |
| Request handler idle % | < 20% | P2 (Medium) |
| Disk usage | > 80% | P1 (High) |
| Network throughput | > 70% capacity | P2 (Medium) |

## Security Checklist

- [ ] TLS enabled for all listeners
- [ ] SASL/SCRAM authentication configured
- [ ] Kafka ACLs implemented (least privilege)
- [ ] Network policies applied
- [ ] Secrets externalized (not in code)
- [ ] Regular security updates scheduled
- [ ] Audit logging enabled
- [ ] Vulnerability scanning in CI/CD
- [ ] Access controls for Kubernetes resources
- [ ] Encryption at rest (if required by compliance)

## Support & Resources

### Documentation
- Strimzi: https://strimzi.io/docs/
- Apache Kafka: https://kafka.apache.org/documentation/
- Confluent Platform: https://docs.confluent.io/

### Community
- Strimzi Slack: https://slack.cncf.io/ (#strimzi)
- Apache Kafka Users: users@kafka.apache.org
- CNCF Slack: Various Kafka channels

### Monitoring Tools
- Prometheus: https://prometheus.io/
- Grafana: https://grafana.com/
- Kafka UI: https://github.com/provectus/kafka-ui

### Load Testing
- K6: https://k6.io/
- JMeter: https://jmeter.apache.org/
- Kafka Perf Test: Built into Kafka

## Next Steps

1. **Review** the production guide: `docs/KAFKA_K8S_PRODUCTION_GUIDE.md`
2. **Customize** the manifests in `k8s/kafka/` for your environment
3. **Deploy** to a test/staging cluster first
4. **Validate** performance and reliability
5. **Plan** the production migration
6. **Execute** the migration with proper change management
7. **Monitor** and optimize post-migration

## Questions to Answer Before Production

1. **Infrastructure**: Which cloud provider? Which Kubernetes distribution?
2. **Storage**: What storage class provides the right IOPS/cost balance?
3. **Networking**: Do you need external access? Which ingress controller?
4. **Security**: What compliance requirements? (PCI, HIPAA, SOC2, etc.)
5. **Backup**: What's your RTO/RPO? Where to store backups?
6. **Monitoring**: Existing monitoring stack? Where to send alerts?
7. **CI/CD**: How to automate deployments? GitOps with ArgoCD/Flux?
8. **Team**: Who's on-call? What's the escalation process?

## Summary

This production migration guide provides:

✅ Comprehensive documentation of production considerations
✅ Production-ready Kubernetes manifests using industry-standard Strimzi
✅ Security hardening with TLS + SASL/SCRAM + ACLs
✅ High availability configuration with anti-affinity and PDBs
✅ Monitoring integration with Prometheus
✅ Step-by-step deployment guide
✅ Migration strategy options
✅ Post-migration checklist

You now have everything needed to deploy Kafka on Kubernetes in production. Start with a test cluster, validate the setup, then proceed to production with confidence.

**Good luck with your production deployment! 🚀**
