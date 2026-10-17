# Multi-stage Dockerfile for Library Management System
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/library-management-system-*.jar app.jar
EXPOSE 8080
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=library_db
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=postgres
ENTRYPOINT ["java", "-jar", "app.jar"]
