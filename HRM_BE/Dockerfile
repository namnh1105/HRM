# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Create application user
RUN addgroup -g 1001 -S worksphere && \
    adduser -S worksphere -u 1001 -G worksphere

# Create application directory
RUN mkdir -p /opt/worksphere/logs && \
    chown -R worksphere:worksphere /opt/worksphere && \
    chmod -R 755 /opt/worksphere && \
    chmod -R 775 /opt/worksphere/logs

WORKDIR /opt/worksphere

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar /opt/worksphere/worksphere.jar

# Change to non-root user
USER worksphere

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "/opt/worksphere/worksphere.jar"]
