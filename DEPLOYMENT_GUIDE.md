# Deployment Guide Index

## 📚 Core Documentation

| Document | Purpose |
|----------|---------|
| [DEPLOYMENT_ARCHITECTURE.md](DEPLOYMENT_ARCHITECTURE.md) | System design, architecture diagram, multi-environment strategy |
| [DEPLOYMENT_RUNBOOK.md](DEPLOYMENT_RUNBOOK.md) | Step-by-step procedures, incident response, troubleshooting |
| [DEPLOYMENT_SUMMARY.txt](DEPLOYMENT_SUMMARY.txt) | Quick reference of all artifacts and commands |

---

## 🐳 Container & Orchestration

### Docker
- **Dockerfile** – Multi-stage build (builder + runtime), health checks, optimized JVM flags
- **docker-compose.yml** – Local development: app + PostgreSQL with volumes

### Kubernetes
- **kubernetes/customer-service-deployment.yaml**
  - Namespace, ConfigMap, Secret
  - Deployment (3 replicas, rolling update)
  - Service (ClusterIP)
  - HorizontalPodAutoscaler (3-10 pods)
  - ServiceAccount & RBAC
  
- **kubernetes/ingress-pdb.yaml**
  - Ingress (TLS, path-based routing)
  - Pod Disruption Budget (min 2 available)
  - ExternalName Service for PostgreSQL

---

## ☁️ Cloud Infrastructure

### AWS CloudFormation
- **cloudformation/customer-service-stack.yaml**
  - VPC with public/private subnets (Multi-AZ)
  - RDS PostgreSQL (configurable Multi-AZ)
  - ECS Fargate cluster & service
  - Application Load Balancer
  - Auto Scaling (CPU-based)
  - CloudWatch Logs integration
  - IAM roles and security groups

---

## 🔄 CI/CD Pipeline

### GitHub Actions
- **.github/workflows/ci-cd-pipeline.yml**
  - **Build stage**: Maven clean package, JUnit tests, SonarQube scan
  - **Push stage**: Docker build & push to GHCR
  - **Deploy Staging**: Automatic K8s rollout (develop branch)
  - **Deploy Prod**: Manual approval (main branch)
  - **Health checks**: Post-deployment validation
  - **Slack notifications**: Success/failure alerts

---

## 🚀 Deployment Scripts

| Script | Function |
|--------|----------|
| `scripts/deploy.sh` | Deploy/update K8s deployment with specified image tag |
| `scripts/rollback.sh` | Rollback to previous K8s revision with confirmation |
| `scripts/health-check.sh` | Check pod status, resource usage, logs, health endpoints |

**Usage:**
```bash
# Deploy
bash scripts/deploy.sh v1.0.0 ghcr.io your-org/customer-service

# Rollback
bash scripts/rollback.sh

# Monitor
bash scripts/health-check.sh
```

---

## 📊 Monitoring & Observability

### Prometheus
- **monitoring/prometheus.yml** – Scrape configs for app metrics, K8s pods/nodes, PostgreSQL exporter
- **monitoring/alert-rules.yml** – Alert rules:
  - Error rate > 5%
  - Latency p95 > 500ms
  - Pod restarts
  - Connection pool exhaustion
  - High memory/CPU
  - Deployment replica mismatch

### Logging
- **logging/fluent-bit-configmap.yaml** – Log forwarding pipeline:
  - Tail application logs (Java parser)
  - Kubernetes metadata enrichment
  - Outputs: Elasticsearch, Loki, AWS CloudWatch
  - Configurable parsers (Java, JSON, Docker)

---

## 🔐 Security & Secrets

### Configuration Management
- **Application**: Environment variables via ConfigMap/Secret
  - DB connection details
  - JWT secret (high entropy)
  
- **AWS**: Secrets Manager for sensitive data
- **K8s**: Sealed Secrets or external secret operator recommended

### Network Security
- VPC with private subnets for app/DB
- Security groups restrict traffic
- TLS/SSL on load balancer
- Container image scanning in CI/CD

---

## 📈 Scaling Strategy

### Horizontal Scaling
- Kubernetes HPA: 3-10 pods based on CPU (70%) & memory (80%)
- AWS Auto Scaling: 1-10 ECS tasks based on CPU (70%)
- Load balancer distributes traffic

### Vertical Scaling
- Adjust resource requests/limits in K8s manifests
- Modify ECS task CPU/memory
- RDS instance class upgrade (scheduled)

---

## 🔄 Deployment Strategies

### Rolling Update (Default)
- Kubernetes: max surge 1, max unavailable 0
- Gradually replace old pods with new
- Zero-downtime deployments
- Automatic rollback on health check failure

### Blue-Green Deployment
- Deploy new version (green) alongside current (blue)
- Switch load balancer traffic to green
- Keep blue running for instant rollback
- Manual cutoff after validation

**Implemented in**: DEPLOYMENT_RUNBOOK.md (Production Deployment section)

---

## 🚨 Disaster Recovery

### Backup Strategy
- **Database**: Daily RDS snapshots (30-day retention)
- **Application**: Immutable Docker images tagged with version
- **Configuration**: Version control in Git

### Recovery Procedures
- **RTO** (Recovery Time): < 15 minutes
- **RPO** (Recovery Point): < 1 hour
- **Rollback**: Automated (previous K8s revision or ECS task definition)
- **Database restore**: Manual from snapshot (documented in runbook)

---

## 📋 Deployment Checklist

Before deploying to production:

- [ ] All tests passing (unit, integration, e2e)
- [ ] Code review approved
- [ ] Security scan passed (SonarQube, container scan)
- [ ] Database migration tested on staging
- [ ] Secrets securely configured
- [ ] Monitoring/alerting enabled
- [ ] Runbook reviewed
- [ ] Rollback plan prepared
- [ ] Team notified
- [ ] Deployment window scheduled
- [ ] Health checks post-deployment

---

## 🎯 Deployment Environments

| Environment | Cluster | Replicas | Auto-scale | DB | Retention |
|-------------|---------|----------|------------|----|-----------|----|
| dev | local/minikube | 1 | no | H2 | N/A |
| staging | K8s/ECS small | 2 | yes | RDS | 7 days |
| prod | K8s/ECS multi-AZ | 3 | yes | RDS multi-AZ | 30 days |

---

## 🔗 Command Reference

### Local Development
```bash
docker-compose up
# App at http://localhost:8080
# Postgres at localhost:5432
```

### Kubernetes
```bash
# Deploy
kubectl apply -f kubernetes/

# Monitor
kubectl get pods -n customer-service
kubectl logs -n customer-service -l app=customer-service

# Scale
kubectl scale deployment customer-service --replicas=5 -n customer-service

# Rollback
kubectl rollout undo deployment/customer-service -n customer-service
```

### AWS ECS
```bash
# Update service
aws ecs update-service \
  --cluster customer-service-prod \
  --service customer-service \
  --force-new-deployment

# View logs
aws logs tail /ecs/customer-service-prod --follow
```

---

## 📞 Support & Escalation

- **On-Call Slack**: #customer-service-oncall
- **Wiki**: https://confluence.example.com/customer-service
- **Issue Tracking**: https://jira.example.com/browse/CS
- **Status Page**: https://status.example.com

---

## 📝 Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-27 | Initial deployment infrastructure |

---

*Deployment guide generated on 2026-02-27 for customer-service v1.0.0*
