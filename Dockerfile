# =============================================================
# Dockerfile for CakeBox Spring Boot Backend
# Place this file in: cakebox-backend/Dockerfile
# =============================================================

# STAGE 1: Build the JAR using Maven + Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first (so Maven downloads dependencies only when pom changes)
COPY pom.xml .

# Download all dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR, skip tests
RUN mvn clean package -DskipTests

# =============================================================
# STAGE 2: Run the JAR using lightweight Java 21 image
# =============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]