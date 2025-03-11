#!/bin/sh
set -e

echo "Waiting for Keycloak to be ready..."
until $(curl --output /dev/null --silent --fail http://keycloak:8080/health/ready); do
  echo "Keycloak not ready yet - sleeping"
  sleep 5
done
echo "Keycloak is up - starting auth service"

exec java -jar app.jar