FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy and build the common-lib first
COPY common-lib /app/common-lib
RUN cd /app/common-lib && mvn clean install -DskipTests

# Now copy and build the auth-service
COPY auth-service /app/auth-service
WORKDIR /app/auth-service
RUN mvn clean package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Add wait-for-it script to ensure Keycloak is ready
RUN apk add --no-cache curl

# Create a startup script
COPY --from=build /app/auth-service/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8081
ENTRYPOINT ["/docker-entrypoint.sh"]