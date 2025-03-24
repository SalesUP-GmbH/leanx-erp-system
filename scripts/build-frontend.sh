##########################################################
# File: build-frontend.sh                                #
# Version: 1.0                                           #
# Author: Janis Grüttmüller on 11.03.2025                #
# Description: script to build the docker frontend image #
# of the LeanX ERP-System and push it to dockerhub       #
#                                                        #
# change history:                                        #
# 24.03.2025 - initial version                           #
##########################################################

#!/bin/bash
set -e  # Exit if any command fails

# Docker login
echo "Logging in to Docker Hub..."
echo "$DOCKERHUB_ACCESS_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

# Build frontend Docker image
echo "Building frontend Docker image..."
docker build -t "$DOCKERHUB_USERNAME/leanx-erp-system-frontend:latest" -t "$DOCKERHUB_USERNAME/leanx-erp-system-frontend:${GITHUB_RUN_NUMBER}" \
    -f frontend/Dockerfile frontend

# Push Docker images to Docker Hub
docker push "$DOCKERHUB_USERNAME/leanx-erp-system-frontend:latest"
docker push "$DOCKERHUB_USERNAME/leanx-erp-system-frontend:${GITHUB_RUN_NUMBER}"

echo "Build process completed successfully!"