#!/bin/bash
# Created by nguyeninnov8 on 2025-03-06 08:25:56

echo "Waiting for Keycloak to be ready..."
until curl -s http://keycloak:8080/health/ready > /dev/null; do
  echo "Keycloak not ready yet - sleeping"
  sleep 5
done
echo "Keycloak is ready - proceeding with setup"

echo "Keycloak is up, configuring..."

# Get admin token - note the updated endpoint without /auth prefix
ADMIN_TOKEN=$(curl -s -X POST \
  http://keycloak:8080/realms/master/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=admin-cli&username=admin&password=admin' \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to get admin token"
  exit 1
fi

# Create realm if it doesn't exist - note the updated URL without /auth prefix
REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
  http://keycloak:8080/admin/realms/restaurant-realm \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if [ "$REALM_EXISTS" -ne "200" ]; then
    echo "Creating realm restaurant-realm..."
    curl -s -X POST \
      http://keycloak:8080/admin/realms \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"realm":"restaurant-realm","enabled":true}'
fi

# Create client if it doesn't exist
CLIENT_EXISTS=$(curl -s \
  http://keycloak:8080/admin/realms/restaurant-realm/clients \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | grep -c '"clientId":"auth-service"')

if [ "$CLIENT_EXISTS" -eq "0" ]; then
    echo "Creating client auth-service..."
    curl -s -X POST \
      http://keycloak:8080/admin/realms/restaurant-realm/clients \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"clientId":"auth-service","enabled":true,"clientAuthenticatorType":"client-secret","secret":"your-client-secret","serviceAccountsEnabled":true,"directAccessGrantsEnabled":true,"authorizationServicesEnabled":true,"redirectUris":["*"]}'
fi

echo "Keycloak setup completed!"