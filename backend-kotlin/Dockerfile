# Stage 1: Build the application using Gradle
FROM gradle:jdk17 as builder

# Copy source code into the image
COPY --chown=gradle:gradle . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Build the jar file
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the final image with the built jar
FROM openjdk:17-jdk-slim

# Create a non-root user
RUN useradd -ms /bin/bash user
USER user
WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/app.jar

# Switch back to the non-root user
USER root

# Set the environment variable pointing to the secret file
ENV GOOGLE_APPLICATION_CREDENTIALS=/etc/secrets/gcpJson

# Expose port 8080 (or whatever port your app uses)
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "/app/app.jar"]
