#!/bin/bash

# Deployment script for Docker Swarm service
# Usage: ./deploy.sh <image_name> <service_name>

set -e  # Exit on any error
set -a  # Export all variables

# Check if required arguments are provided
if [ $# -ne 2 ]; then
    echo "Usage: $0 <image_name> <service_name>"
    echo "Example: $0 myapp:latest my-service"
    exit 1
fi

IMAGE_NAME="$1"
SERVICE_NAME="$2"

echo "Starting deployment..."
echo "Image: $IMAGE_NAME"
echo "Service: $SERVICE_NAME"

# Pull the new image
echo "Pulling new image..."
docker pull "$IMAGE_NAME"

# Update the service with rolling update
echo "Exporting environment variables..."
source /home/administrator/starline/.env.local

echo "Updating service..."
docker service update \
  --image "$IMAGE_NAME" \
  --update-order start-first \
  --update-parallelism 1 \
  --update-delay 10s \
  --update-failure-action rollback \
  --update-monitor 30s \
  --rollback-parallelism 1 \
  --rollback-delay 5s \
  --rollback-failure-action pause \
  --rollback-monitor 30s \
  "$SERVICE_NAME"

echo "Deployment completed."