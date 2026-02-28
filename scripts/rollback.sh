#!/bin/bash

set -e

NAMESPACE="customer-service"
DEPLOYMENT="customer-service"
REVISION="${1:-0}"  # 0 for previous, or specify revision number

echo "========================================="
echo "Rolling back customer-service deployment"
echo "========================================="
echo "Namespace: $NAMESPACE"
echo "Deployment: $DEPLOYMENT"
echo "Target Revision: $REVISION"
echo ""

# Verify kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "ERROR: kubectl is not installed"
    exit 1
fi

# Check current rollout status
echo "Current deployment status:"
kubectl rollout status deployment/$DEPLOYMENT -n $NAMESPACE || true
echo ""

# Show rollout history
echo "Rollout history:"
kubectl rollout history deployment/$DEPLOYMENT -n $NAMESPACE
echo ""

# Confirm before rollback
read -p "Proceed with rollback? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Rollback cancelled"
    exit 0
fi

# Perform rollback
if [ "$REVISION" -eq 0 ]; then
    echo "Rolling back to previous revision..."
    kubectl rollout undo deployment/$DEPLOYMENT -n $NAMESPACE
else
    echo "Rolling back to revision $REVISION..."
    kubectl rollout undo deployment/$DEPLOYMENT --to-revision=$REVISION -n $NAMESPACE
fi

# Wait for rollback to complete
echo "Waiting for rollback to complete..."
kubectl rollout status deployment/$DEPLOYMENT -n $NAMESPACE --timeout=10m

echo ""
echo "Verifying pods..."
READY_PODS=$(kubectl get deployment $DEPLOYMENT -n $NAMESPACE \
    -o jsonpath='{.status.readyReplicas}')
DESIRED_PODS=$(kubectl get deployment $DEPLOYMENT -n $NAMESPACE \
    -o jsonpath='{.spec.replicas}')

if [ "$READY_PODS" -eq "$DESIRED_PODS" ]; then
    echo "✓ All $READY_PODS pods are ready"
    echo ""
    echo "========================================="
    echo "Rollback completed successfully!"
    echo "========================================="
else
    echo "✗ Only $READY_PODS of $DESIRED_PODS pods are ready"
    echo "Rolling back may have failed. Investigate the pods:"
    kubectl describe pods -n $NAMESPACE -l app=$DEPLOYMENT
    exit 1
fi
