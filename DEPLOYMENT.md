# Deployment Guide - Free Hosting Options

This guide provides multiple options for deploying the Social Events microservices architecture for demo purposes at minimal or no cost.

## 🎯 Deployment Strategy Overview

Due to the complexity of this architecture (5 microservices + Kafka + Elasticsearch), we provide several deployment strategies:

1. **Option 1: GitHub Pages + External Services** (Recommended for Demo)
2. **Option 2: Railway.app Free Tier** ($5/month credit)
3. **Option 3: Render.com Free Tier**
4. **Option 4: Oracle Cloud Always Free Tier** (Best for full stack)
5. **Option 5: Fly.io Free Tier**

---

## Option 1: GitHub Pages + External Services (Recommended) 🌟

**Cost**: Free
**Best for**: Quick demo with limited backend functionality

### Architecture:
- **Frontend**: GitHub Pages (free static hosting)
- **Backend API**: Railway.app or Render.com free tier
- **Kafka**: Upstash Kafka (free tier: 10K messages/day)
- **Elasticsearch**: Bonsai Elasticsearch (free tier: 125MB)

### Step 1: Deploy Dashboard to GitHub Pages

1. Enable GitHub Pages in your repository:
   ```bash
   Settings → Pages → Source: GitHub Actions
   ```

2. The CI/CD workflow will automatically deploy on push to main branch

3. Your dashboard will be available at:
   ```
   https://<username>.github.io/<repository-name>/
   ```

### Step 2: Set Up External Services

**Upstash Kafka (Free Tier):**
1. Sign up at https://upstash.com/
2. Create a Kafka cluster
3. Note your bootstrap servers and credentials
4. Free tier: 10,000 messages/day, 100 topics

**Bonsai Elasticsearch (Free Tier):**
1. Sign up at https://bonsai.io/
2. Create a free cluster (Sandbox)
3. Note your cluster URL and credentials
4. Free tier: 125MB storage, 35MB RAM

### Step 3: Deploy Backend Services

**Using Railway.app:**

1. Sign up at https://railway.app/
2. Install Railway CLI:
   ```bash
   npm install -g @railway/cli
   ```

3. Login and deploy:
   ```bash
   railway login
   railway init
   railway up
   ```

4. Set environment variables:
   ```bash
   railway variables set KAFKA_BOOTSTRAP_SERVERS=<upstash-url>
   railway variables set ELASTICSEARCH_HOST=<bonsai-url>
   ```

### Step 4: Update Dashboard API URL

Update the dashboard environment variable:
```bash
# In .github/workflows/ci-cd.yml
env:
  VITE_API_BASE_URL: https://your-api.railway.app
```

### Cost Breakdown:
- GitHub Pages: **Free**
- Upstash Kafka: **Free** (10K msgs/day)
- Bonsai Elasticsearch: **Free** (125MB)
- Railway.app: **$5/month credit** (covers 2-3 services)

**Total: Effectively Free** for demo purposes (Railway credit covers usage)

---

## Option 2: Railway.app Full Deployment

**Cost**: ~$5-10/month (starts with $5 credit)
**Best for**: Simple deployment with good developer experience

### Features:
- ✅ Easy GitHub integration
- ✅ Automatic deployments
- ✅ Free $5 monthly credit
- ✅ PostgreSQL included free
- ❌ No managed Kafka (need external)

### Deployment Steps:

1. **Fork this repository**

2. **Sign up at Railway.app**
   - Connect your GitHub account

3. **Create New Project**
   - Select "Deploy from GitHub repo"
   - Choose your forked repository

4. **Add Services:**
   ```bash
   # Using Railway CLI
   railway add --service event-stream-service
   railway add --service elastic-query-service
   railway add --service dashboard
   ```

5. **Set Environment Variables** (Railway Dashboard):
   ```
   KAFKA_BOOTSTRAP_SERVERS=<upstash-kafka-url>
   ELASTICSEARCH_HOST=<bonsai-elasticsearch-url>
   SPRING_PROFILES_ACTIVE=prod
   ```

6. **Deploy:**
   ```bash
   railway up
   ```

### Railway Configuration:

The `railway.toml` file in the repository root defines the deployment configuration.

---

## Option 3: Render.com Free Tier

**Cost**: Free (with limitations)
**Best for**: Static sites and simple web services

### Features:
- ✅ Free tier for static sites
- ✅ Free tier for web services (750 hrs/month)
- ✅ Automatic deployments from GitHub
- ⚠️ Spins down after 15 min inactivity (slow cold starts)
- ❌ No managed Kafka/Elasticsearch

### Deployment Steps:

1. **Sign up at Render.com**

2. **Create Blueprint**
   - New → Blueprint
   - Connect your GitHub repository
   - Render will detect `render.yaml`

3. **Configure External Services**:
   - Add Upstash Kafka URL
   - Add Bonsai Elasticsearch URL

4. **Deploy**:
   - Render will deploy all services automatically

### Limitations:
- Free web services spin down after 15 minutes of inactivity
- First request after spin-down takes 30-60 seconds (cold start)
- 750 hours/month free (enough for 1 service 24/7)

---

## Option 4: Oracle Cloud Always Free Tier (Best for Full Stack) ⭐

**Cost**: Free forever
**Best for**: Full production-like deployment

### Features:
- ✅ Always free (no time limit)
- ✅ 2 AMD VMs with 1GB RAM each (or 4 ARM VMs with 24GB RAM total!)
- ✅ 200GB block storage
- ✅ 10TB bandwidth/month
- ✅ Can run full Docker Compose stack

### Deployment Steps:

1. **Sign up for Oracle Cloud**: https://www.oracle.com/cloud/free/

2. **Create VM Instance**:
   - Go to Compute → Instances → Create Instance
   - Choose "Always Free Eligible" shape:
     - **Ampere A1 Compute** (ARM): 4 OCPUs, 24GB RAM (Free!)
     - Or AMD: 2 instances × 1GB RAM
   - Choose Ubuntu 22.04 as OS
   - Download SSH key

3. **Connect to VM**:
   ```bash
   ssh -i <your-key.pem> ubuntu@<vm-ip>
   ```

4. **Install Dependencies**:
   ```bash
   # Update system
   sudo apt update && sudo apt upgrade -y

   # Install Docker
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker ubuntu

   # Install Docker Compose
   sudo apt install docker-compose -y

   # Install Java 21
   sudo apt install openjdk-21-jdk -y

   # Install Maven
   sudo apt install maven -y

   # Install Node.js 18
   curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
   sudo apt install -y nodejs
   ```

5. **Clone Repository**:
   ```bash
   git clone https://github.com/<your-username>/<repo-name>.git
   cd <repo-name>
   ```

6. **Start Services**:
   ```bash
   # Make scripts executable
   chmod +x start-all-services.sh stop-all-services.sh

   # Start everything
   ./start-all-services.sh
   ```

7. **Configure Firewall** (Oracle Cloud Console):
   - Networking → Virtual Cloud Networks → Your VCN → Security Lists
   - Add Ingress Rules:
     - Port 3000 (Dashboard)
     - Port 8080 (Event Stream Service)
     - Port 8084 (REST API)
     - Port 5601 (Kibana)

8. **Access Services**:
   ```
   Dashboard: http://<vm-ip>:3000
   API: http://<vm-ip>:8084
   Kibana: http://<vm-ip>:5601
   ```

### Oracle Cloud Advantages:
- ✅ **FREE FOREVER** (not a trial)
- ✅ Run full Kafka + Elasticsearch + all services
- ✅ No cold starts
- ✅ True production environment
- ✅ 4 ARM cores + 24GB RAM (more than enough!)

---

## Option 5: Fly.io Free Tier

**Cost**: Free tier (3 shared VMs, 3GB storage)
**Best for**: Docker-based deployments

### Features:
- ✅ Good Docker support
- ✅ Global edge network
- ✅ Free allowance: 3 VMs, 160GB bandwidth
- ❌ Need external Kafka/Elasticsearch

### Deployment Steps:

1. **Install Fly CLI**:
   ```bash
   curl -L https://fly.io/install.sh | sh
   ```

2. **Login**:
   ```bash
   flyctl auth login
   ```

3. **Deploy Dashboard**:
   ```bash
   cd dashboard-ui
   flyctl launch
   flyctl deploy
   ```

4. **Deploy API Service**:
   ```bash
   cd elastic/elastic-query-service
   flyctl launch
   flyctl deploy
   ```

---

## Recommended Deployment for Demo

### For Quick Demo (30 minutes):
**GitHub Pages + Railway.app + External Services**
- Dashboard on GitHub Pages (free)
- API on Railway (free with credit)
- Kafka on Upstash (free tier)
- Elasticsearch on Bonsai (free tier)

### For Full-Featured Demo (2-3 hours setup):
**Oracle Cloud Always Free Tier**
- Complete infrastructure in one VM
- No service limits
- No cold starts
- Production-like environment
- **100% FREE FOREVER**

---

## Environment Variables for Production

Create a `.env.production` file:

```bash
# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=<your-kafka-url>
KAFKA_SASL_USERNAME=<username>
KAFKA_SASL_PASSWORD=<password>

# Elasticsearch Configuration
ELASTICSEARCH_HOST=<your-elasticsearch-url>
ELASTICSEARCH_USERNAME=<username>
ELASTICSEARCH_PASSWORD=<password>

# Service Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Dashboard Configuration
VITE_API_BASE_URL=https://your-api-url.com
```

---

## Monitoring & Observability

### Free Monitoring Services:

1. **Uptime Robot** (Free):
   - https://uptimerobot.com/
   - Monitor 50 endpoints for free
   - Email alerts on downtime

2. **Better Stack** (Free tier):
   - https://betterstack.com/
   - Log management
   - Uptime monitoring

3. **Sentry** (Free tier):
   - https://sentry.io/
   - Error tracking
   - Performance monitoring

---

## CI/CD with GitHub Actions

The repository includes a complete CI/CD pipeline (`.github/workflows/ci-cd.yml`) that:

1. **Builds** all services on every push
2. **Tests** backend services
3. **Deploys dashboard** to GitHub Pages automatically
4. **Creates Docker images** for backend services

### Required GitHub Secrets:

```
DOCKERHUB_USERNAME=<your-dockerhub-username>
DOCKERHUB_TOKEN=<your-dockerhub-token>
API_BASE_URL=https://your-api-url.com
```

---

## Cost Comparison

| Option | Monthly Cost | Setup Time | Full Stack | Cold Starts |
|--------|-------------|------------|------------|-------------|
| **GitHub Pages + External** | Free* | 30 min | Partial | Yes (Render) |
| **Railway.app** | ~$5-10 | 15 min | No | No |
| **Render.com** | Free | 20 min | Partial | Yes |
| **Oracle Cloud** | **Free** | 2-3 hours | **Yes** | **No** |
| **Fly.io** | Free* | 30 min | Partial | No |

\* Free with limitations

---

## Troubleshooting

### Dashboard can't connect to API:
```bash
# Check CORS configuration in elastic-query-service
# Update application.yml with your dashboard URL
```

### Out of memory on free tier:
```bash
# Reduce JVM heap size
ENV JAVA_OPTS="-Xms256m -Xmx512m"
```

### Railway/Render cold starts:
```bash
# Use external uptime monitor to ping every 5 minutes
# Or upgrade to paid tier
```

---

## Next Steps

1. Choose your deployment option
2. Set up external services (Kafka, Elasticsearch if needed)
3. Configure environment variables
4. Deploy using the provided scripts
5. Set up monitoring
6. Share your demo URL!

## Support

For deployment issues:
- Check service logs in your platform dashboard
- Verify environment variables are set correctly
- Test locally with `./start-all-services.sh` first
- Check firewall/security group rules

---

**Recommendation**: For a true demo with all features working, use **Oracle Cloud Always Free Tier**. It provides enough resources to run the complete stack including Kafka and Elasticsearch, all for **free forever**.
