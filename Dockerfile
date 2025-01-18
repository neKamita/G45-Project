# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Install Redis
RUN apt-get update && \
    apt-get install -y redis-server && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Redis configuration
COPY redis.conf /etc/redis/redis.conf

# Expose ports for both Spring Boot and Redis
EXPOSE 8080 6379

# Start both Redis and Spring Boot application
CMD redis-server /etc/redis/redis.conf --daemonize yes && \
    java -jar app.jar
