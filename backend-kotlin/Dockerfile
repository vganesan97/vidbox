# Use the official Gradle image as a base image
FROM gradle:jdk17 as builder

# Copy source code into the image
COPY --chown=gradle:gradle . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Use the Gradle wrapper to build the jar file
RUN ./gradlew bootJar --no-daemon

# Start with OpenJDK image
FROM openjdk:17-jdk-slim

# Create a non-root user
RUN useradd -ms /bin/bash user
USER user
WORKDIR /app

# Copy the built jar file from the builder image
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/app.jar

ARG VIDBOX_JSON
ENV GOOGLE_APPLICATION_CREDENTIALS=$VIDBOX_JSON

#ENV GOOGLE_APPLICATION_CREDENTIALS=vidbox.json

COPY $VIDBOX_JSON /app/vidbox.json

# Expose port 8080 (or whatever port your app uses)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
