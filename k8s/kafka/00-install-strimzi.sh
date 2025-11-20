#!/bin/bash
set -e

echo "========================================="
echo "Installing Strimzi Kafka Operator"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}kubectl is not installed. Please install kubectl first.${NC}"
    exit 1
fi

if ! command -v helm &> /dev/null; then
    echo -e "${RED}helm is not installed. Please install helm first.${NC}"
    exit 1
fi

# Check cluster connectivity
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${RED}Cannot connect to Kubernetes cluster. Please check your kubeconfig.${NC}"
    exit 1
fi

echo -e "${GREEN}Prerequisites OK${NC}"

# Create kafka namespace
echo -e "\n${YELLOW}Creating kafka namespace...${NC}"
kubectl create namespace kafka --dry-run=client -o yaml | kubectl apply -f -
kubectl label namespace kafka name=kafka --overwrite

# Add Strimzi Helm repository
echo -e "\n${YELLOW}Adding Strimzi Helm repository...${NC}"
helm repo add strimzi https://strimzi.io/charts/
helm repo update

# Install Strimzi operator
echo -e "\n${YELLOW}Installing Strimzi operator...${NC}"
helm upgrade --install strimzi-kafka-operator strimzi/strimzi-kafka-operator \
  --namespace kafka \
  --version 0.38.0 \
  --set watchNamespaces="{kafka}" \
  --set logLevel=INFO \
  --wait

# Wait for operator to be ready
echo -e "\n${YELLOW}Waiting for operator to be ready...${NC}"
kubectl wait --for=condition=ready pod -l name=strimzi-cluster-operator -n kafka --timeout=300s

# Create metrics ConfigMaps
echo -e "\n${YELLOW}Creating metrics ConfigMaps...${NC}"
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: kafka-metrics
  namespace: kafka
data:
  kafka-metrics-config.yml: |
    lowercaseOutputName: true
    rules:
    - pattern: kafka.server<type=(.+), name=(.+), clientId=(.+), topic=(.+), partition=(.*)><>Value
      name: kafka_server_\$1_\$2
      type: GAUGE
      labels:
       clientId: "\$3"
       topic: "\$4"
       partition: "\$5"
    - pattern: kafka.server<type=(.+), name=(.+), clientId=(.+), brokerHost=(.+), brokerPort=(.+)><>Value
      name: kafka_server_\$1_\$2
      type: GAUGE
      labels:
       clientId: "\$3"
       broker: "\$4:\$5"
    - pattern: kafka.server<type=(.+), cipher=(.+), protocol=(.+), listener=(.+), networkProcessor=(.+)><>connections
      name: kafka_server_\$1_connections_tls_info
      type: GAUGE
      labels:
        cipher: "\$2"
        protocol: "\$3"
        listener: "\$4"
        networkProcessor: "\$5"
    - pattern: kafka.server<type=(.+), clientSoftwareName=(.+), clientSoftwareVersion=(.+), listener=(.+), networkProcessor=(.+)><>connections
      name: kafka_server_\$1_connections_software
      type: GAUGE
      labels:
        clientSoftwareName: "\$2"
        clientSoftwareVersion: "\$3"
        listener: "\$4"
        networkProcessor: "\$5"
    - pattern: "kafka.server<type=(.+), listener=(.+), networkProcessor=(.+)><>(.+):"
      name: kafka_server_\$1_\$4
      type: GAUGE
      labels:
       listener: "\$2"
       networkProcessor: "\$3"
    - pattern: kafka.server<type=(.+), listener=(.+), networkProcessor=(.+)><>(.+)
      name: kafka_server_\$1_\$4
      type: GAUGE
      labels:
       listener: "\$2"
       networkProcessor: "\$3"
    - pattern: kafka.(\w+)<type=(.+), name=(.+)Percent\w*><>MeanRate
      name: kafka_\$1_\$2_\$3_percent
      type: GAUGE
    - pattern: kafka.(\w+)<type=(.+), name=(.+)Percent\w*><>Value
      name: kafka_\$1_\$2_\$3_percent
      type: GAUGE
    - pattern: kafka.(\w+)<type=(.+), name=(.+)Percent\w*, (.+)=(.+)><>Value
      name: kafka_\$1_\$2_\$3_percent
      type: GAUGE
      labels:
        "\$4": "\$5"
  zookeeper-metrics-config.yml: |
    lowercaseOutputName: true
    rules:
    - pattern: "org.apache.ZooKeeperService<name0=ReplicatedServer_id(\\d+)><>(\\w+)"
      name: "zookeeper_\$2"
      type: GAUGE
    - pattern: "org.apache.ZooKeeperService<name0=ReplicatedServer_id(\\d+), name1=replica.(\\d+)><>(\\w+)"
      name: "zookeeper_\$3"
      type: GAUGE
      labels:
        replicaId: "\$2"
    - pattern: "org.apache.ZooKeeperService<name0=ReplicatedServer_id(\\d+), name1=replica.(\\d+), name2=(\\w+)><>(Packets\\w+)"
      name: "zookeeper_\$4"
      type: GAUGE
      labels:
        replicaId: "\$2"
        memberType: "\$3"
    - pattern: "org.apache.ZooKeeperService<name0=ReplicatedServer_id(\\d+), name1=replica.(\\d+), name2=(\\w+)><>(\\w+)"
      name: "zookeeper_\$4"
      type: GAUGE
      labels:
        replicaId: "\$2"
        memberType: "\$3"
    - pattern: "org.apache.ZooKeeperService<name0=ReplicatedServer_id(\\d+), name1=replica.(\\d+), name2=(\\w+), name3=(\\w+)><>(\\w+)"
      name: "zookeeper_\$4_\$5"
      type: GAUGE
      labels:
        replicaId: "\$2"
        memberType: "\$3"
EOF

echo -e "\n${GREEN}Strimzi operator installation complete!${NC}"
echo -e "${GREEN}You can now deploy your Kafka cluster using:${NC}"
echo -e "${YELLOW}  kubectl apply -f 01-kafka-cluster.yaml${NC}"
echo -e "${YELLOW}  kubectl wait kafka/social-events-cluster --for=condition=Ready --timeout=600s -n kafka${NC}"
