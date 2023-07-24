# Use the official Gradle image as a base image
FROM gradle:jdk17 as builder

# Copy your source code into the image
COPY --chown=gradle:gradle . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Run the build task
RUN gradle bootJar --no-daemon

# Use the official OpenJDK image as a base image
FROM openjdk:17-jdk-slim

# Copy the jar file from the builder image
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/app.jar

# Expose port 8080 (or whatever port your app uses)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]
