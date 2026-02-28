# Cloud Deployment Runbook

## Pre-Deployment Checklist

- [ ] All tests passing (unit, integration, e2e)
- [ ] Code review approved
- [ ] Security scan passed
- [ ] Database migration script tested
- [ ] Secrets securely configured
- [ ] Monitoring & alerting enabled
- [ ] Backup taken of current database
- [ ] Runbook reviewed with team
- [ ] Rollback plan validated

---

## AWS ECS Deployment (Fargate)

### Prerequisites
- AWS CLI configured
- ECR repository created
- RDS PostgreSQL instance running
- ALB configured with target group

### Steps

1. **Build and push Docker image**
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
   docker build -t customer-service:latest .
   docker tag customer-service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/customer-service:latest
   docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/customer-service:latest
   ```

2. **Update ECS service**
   ```bash
   aws ecs update-service \
     --cluster customer-service-cluster \
     --service customer-service \
     --force-new-deployment \
     --region us-east-1
   ```

3. **Monitor deployment**
   ```bash
   aws ecs describe-services --cluster customer-service-cluster --services customer-service --region us-east-1
   ```

---

## Kubernetes Deployment (EKS/GKE/AKS)

### Prerequisites
- kubectl configured with cluster access
- Docker image in registry
- ConfigMaps and Secrets created

### Steps

1. **Apply manifests**
   ```bash
   kubectl apply -f kubernetes/customer-service-deployment.yaml
   kubectl apply -f kubernetes/ingress-pdb.yaml
   ```

2. **Monitor rollout**
   ```bash
   kubectl rollout status deployment/customer-service -n customer-service --timeout=10m
   ```

3. **Verify deployment**
   ```bash
   kubectl get pods -n customer-service
   kubectl logs -n customer-service -l app=customer-service --tail=50
   ```

---

## Production Deployment (Blue-Green)

### Phase 1: Green Deployment
1. Deploy new version to green environment
2. Run smoke tests
3. Verify all systems operational

### Phase 2: Traffic Switch
1. Update load balancer to route to green
2. Monitor error rates and latency
3. Watch for anomalies (20 minutes minimum)

### Phase 3: Cleanup
1. Keep blue running for 1 hour
2. Drain connections gracefully
3. Terminate blue environment

### Rollback
If issues detected during Phase 2:
1. Immediately switch traffic back to blue
2. Investigate root cause
3. Document findings

---

## Database Migration

### Pre-Migration
```bash
# Backup current database
pg_dump -h postgres.example.com -U postgres customerdb > backup_$(date +%s).sql

# Test migrations on staging first
```

### During Migration
```bash
# Apply Flyway/Liquibase migrations (automatic with ddl-auto=update)
# Or manually:
psql -h postgres.example.com -U postgres customerdb < migration.sql
```

### Post-Migration
```bash
# Verify schema
psql -h postgres.example.com -U postgres customerdb -c "\dt"

# Run validation queries
SELECT COUNT(*) FROM customers;
```

---

## Incident Response

### High Error Rate (>5%)
1. Check application logs: `kubectl logs -n customer-service deployment/customer-service`
2. Verify database connectivity
3. Check memory/CPU resources
4. If unresolved: trigger rollback

### High Latency (p95 > 500ms)
1. Check database query performance
2. Examine connection pool status
3. Monitor network latency
4. Consider scaling horizontally

### Pod Crashes
1. Inspect pod events: `kubectl describe pod -n customer-service <pod-name>`
2. Check application logs for errors
3. Verify resource limits aren't exceeded
4. Check for configuration/secrets issues

### Database Connection Failures
1. Verify database is accessible
2. Check credentials in secrets
3. Verify security group rules
4. Check connection pool settings in application

---

## Monitoring & Logging Access

### Prometheus Metrics
```bash
# Port-forward to Prometheus
kubectl port-forward -n monitoring svc/prometheus 9090:9090

# Access at http://localhost:9090
# Query: rate(http_requests_total[5m])
```

### Grafana Dashboards
```bash
kubectl port-forward -n monitoring svc/grafana 3000:3000
# Access at http://localhost:3000 (admin/password)
```

### ELK Stack / Loki Logs
```bash
# Kibana
kubectl port-forward -n logging svc/kibana 5601:5601

# Grafana Loki
# Use Grafana Explore with Loki datasource
```

---

## Rollback Procedures

### Automated Rollback (Kubernetes)
```bash
# Show revision history
kubectl rollout history deployment/customer-service -n customer-service

# Rollback to previous version
kubectl rollout undo deployment/customer-service -n customer-service

# Rollback to specific revision
kubectl rollout undo deployment/customer-service -n customer-service --to-revision=5

# Watch rollout
kubectl rollout status deployment/customer-service -n customer-service
```

### Automated Rollback (AWS ECS)
```bash
# Get previous task definition
aws ecs describe-service --cluster customer-service-cluster --services customer-service --region us-east-1

# Update service with previous task definition
aws ecs update-service \
  --cluster customer-service-cluster \
  --service customer-service \
  --task-definition customer-service:N \
  --region us-east-1
```

### Database Rollback
```bash
# Restore from backup (last resort)
psql -h postgres.example.com -U postgres customerdb < backup_TIMESTAMP.sql
```

---

## Post-Deployment Validation

1. **Health Check**
   ```bash
   curl -H "Authorization: Bearer $(curl -X POST http://api.example.com/api/v1/auth/login -d '{"username":"admin","password":"admin123"}' | jq -r .token)" \
     http://api.example.com/api/v1/customers
   ```

2. **Performance Baseline**
   - Verify p95 latency < 500ms
   - Error rate < 1%

3. **Log Review**
   - Check for authentication errors
   - Look for database warnings
   - Verify no unexpected exceptions

4. **Team Notification**
   - Slack message with deployment status
   - Alert monitoring team
   - Update status page

---

## Troubleshooting

| Issue | Cause | Resolution |
|-------|-------|-----------|
| Pods stuck in `Pending` | Insufficient resources | Scale cluster or reduce limits |
| CrashLoopBackOff | App error | Check logs: `kubectl logs` |
| Deployment timeout | Readiness probe failing | Increase initial delay or fix health endpoint |
| High memory | Memory leak | Restart pods, investigate code |
| DB connection refused | Network/credentials | Verify security groups, secrets |

---

## Contact & Escalation

- **On-Call Engineer**: +1-XXX-XXX-XXXX
- **Slack Channel**: #customer-service-deployments
- **Page Duty**: https://incident-commander.pagerduty.com
- **War Room**: https://zoom.us/j/XXXXXXXXX

---

*Runbook version 1.0 - Last updated: 2026-02-27*
