# syntax = docker/dockerfile:1.2
# Use the official Gradle image as a base image
FROM gradle:jdk17 as builder

# Copy source code into the image
COPY --chown=gradle:gradle . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Use the Gradle wrapper to build the jar file
RUN --mount=type=secret,id=gcpJson,dst=/etc/secrets/gcpJson ./gradlew bootJar --no-daemon

# Switch to root user to access the secret file
USER root

# Copy the secret file into the container
COPY $gcpJson /etc/secrets/gcpJson

# Set the correct permissions for the secret file
RUN chmod 644 /etc/secrets/gcpJson

# Switch back to the gradle user
USER gradle

# Start with OpenJDK image
FROM openjdk:17-jdk-slim

# Create a non-root user
RUN useradd -ms /bin/bash user
USER user
WORKDIR /app

# Copy the built jar file from the builder image
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/app.jar

# Expose port 8080 (or whatever port your app uses)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
