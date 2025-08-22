# Multi-stage build for optimized image size
FROM eclipse-temurin:21-jdk-alpine AS build

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN mvn dependency:resolve dependency:resolve-sources -B

# Copy source code
COPY src ./src

# Generate RSA key pairs
RUN mkdir -p src/main/resources/certs && \
    openssl genrsa -out src/main/resources/certs/keypair.pem 2048 && \
    openssl rsa -in src/main/resources/certs/keypair.pem -pubout -out src/main/resources/certs/public.pem && \
    openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in src/main/resources/certs/keypair.pem -out src/main/resources/certs/private.pem && \
    rm src/main/resources/certs/keypair.pem

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S citydata && adduser -u 1001 -S citydata -G citydata

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to citydata user
RUN chown -R citydata:citydata /app

# Switch to non-root user
USER citydata

# Expose port (default Spring Boot port)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]