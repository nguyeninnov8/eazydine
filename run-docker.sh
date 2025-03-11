#!/bin/bash
# Created by nguyeninnov8 on 2025-03-06 07:43:59

echo "Building and starting all services with Docker Compose..."

# Build and start the services
docker-compose up -d --build

echo "Services are starting up..."
echo "Keycloak will be available at http://localhost:8180"
echo "Auth Service will be available at http://localhost:8081"