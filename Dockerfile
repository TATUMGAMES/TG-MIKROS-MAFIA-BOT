# Dockerfile for TG-MIKROS Discord Bot
# Multi-stage build for optimized image size

# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradlew ./
COPY gradle/ gradle/

# Copy source code
COPY src/ ./src/

# Build the application
RUN chmod +x gradlew && ./gradlew build -x test

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Set environment variables (can be overridden)
# ENV DISCORD_BOT_TOKEN=""
# ENV BOT_OWNER_ID=""

# Expose any ports if needed in the future
# EXPOSE 8080

# Health check (optional)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#   CMD pgrep -f "java.*app.jar" || exit 1

# Run the bot
CMD ["java", "-jar", "app.jar"]





