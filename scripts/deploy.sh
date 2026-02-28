#!/bin/bash

set -e

NAMESPACE="customer-service"
DEPLOYMENT="customer-service"
IMAGE_TAG="${1:-latest}"
REGISTRY="${2:-ghcr.io}"
REPO="${3:-your-org/customer-service}"
TIMEOUT="10m"

echo "========================================="
echo "Deploying customer-service to Kubernetes"
echo "========================================="
echo "Namespace: $NAMESPACE"
echo "Deployment: $DEPLOYMENT"
echo "Image Tag: $IMAGE_TAG"
echo "Registry: $REGISTRY"
echo ""

# Verify kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "ERROR: kubectl is not installed"
    exit 1
fi

# Check if namespace exists
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    echo "Creating namespace: $NAMESPACE"
    kubectl create namespace "$NAMESPACE"
fi

# Update image in deployment
echo "Updating deployment image..."
kubectl set image deployment/$DEPLOYMENT $DEPLOYMENT=$REGISTRY/$REPO:$IMAGE_TAG \
    -n "$NAMESPACE" || {
    echo "ERROR: Failed to update deployment image"
    exit 1
}

# Wait for rollout to complete
echo "Waiting for rollout to complete (timeout: $TIMEOUT)..."
if kubectl rollout status deployment/$DEPLOYMENT \
    -n "$NAMESPACE" --timeout="$TIMEOUT"; then
    echo "✓ Deployment successful"
else
    echo "✗ Deployment failed or timed out"
    exit 1
fi

# Verify pods are running
echo "Verifying pods..."
READY_PODS=$(kubectl get deployment $DEPLOYMENT -n $NAMESPACE \
    -o jsonpath='{.status.readyReplicas}')
DESIRED_PODS=$(kubectl get deployment $DEPLOYMENT -n $NAMESPACE \
    -o jsonpath='{.spec.replicas}')

if [ "$READY_PODS" -eq "$DESIRED_PODS" ]; then
    echo "✓ All $READY_PODS pods are ready"
else
    echo "✗ Only $READY_PODS of $DESIRED_PODS pods are ready"
    kubectl describe pods -n $NAMESPACE -l app=$DEPLOYMENT
    exit 1
fi

# Get service info
echo ""
echo "Service Information:"
kubectl get svc -n "$NAMESPACE" -l app="$DEPLOYMENT" -o wide

echo ""
echo "========================================="
echo "Deployment completed successfully!"
echo "========================================="
