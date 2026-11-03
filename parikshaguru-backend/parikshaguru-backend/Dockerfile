# Stage 1: Build the application with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom.xml and dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application with Eclipse Temurin Java 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /build/target/parikshaguru-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
