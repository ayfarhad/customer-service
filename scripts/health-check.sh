#!/bin/bash

NAMESPACE="customer-service"
DEPLOYMENT="customer-service"
SERVICE="customer-service"

echo "========================================="
echo "Health Check & Monitoring Report"
echo "========================================="
echo ""

# Check pod status
echo "Pod Status:"
kubectl get pods -n $NAMESPACE -l app=$DEPLOYMENT -o wide
echo ""

# Check deployment status
echo "Deployment Status:"
kubectl get deployment -n $NAMESPACE $DEPLOYMENT
echo ""

# Check service
echo "Service Status:"
kubectl get svc -n $NAMESPACE $SERVICE
echo ""

# Check pod events
echo "Recent Pod Events:"
kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -10
echo ""

# Check resource usage
echo "Pod Resource Usage:"
kubectl top pods -n $NAMESPACE -l app=$DEPLOYMENT 2>/dev/null || echo "Metrics not available"
echo ""

# Check logs from latest pod
echo "Latest Pod Logs (last 50 lines):"
LATEST_POD=$(kubectl get pods -n $NAMESPACE -l app=$DEPLOYMENT -o jsonpath='{.items[0].metadata.name}')
if [ -n "$LATEST_POD" ]; then
    kubectl logs -n $NAMESPACE "$LATEST_POD" --tail=50
else
    echo "No pods found"
fi
echo ""

# Health endpoint check
echo "Health Endpoint Check:"
SVCIP=$(kubectl get svc -n $NAMESPACE $SERVICE -o jsonpath='{.spec.clusterIP}')
if [ -n "$SVCIP" ]; then
    kubectl run -n $NAMESPACE health-check --image=curlimages/curl:latest --rm -i --restart=Never -- \
        curl -s http://$SVCIP:80/api/v1/customers || echo "Health check failed"
fi
echo ""

echo "========================================="
echo "Report completed"
echo "========================================="
