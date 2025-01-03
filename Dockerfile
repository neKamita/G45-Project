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

# Environment variables for database connection (to be configured in Render.com)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}
ENV SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
ENV SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
