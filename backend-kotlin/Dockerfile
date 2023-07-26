# Use the official Gradle image as a base image
FROM gradle:jdk17 as builder

# Copy your source code into the image
COPY --chown=gradle:gradle . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Run the build task
RUN gradle bootJar --no-daemon

FROM openjdk:17-jdk-slim

# Copy the jar file from the builder image
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/app.jar

COPY vidbox-7d2c1-firebase-adminsdk-akp4p-f90c0efd75.json /app/vidbox-7d2c1-firebase-adminsdk-akp4p-f90c0efd75.json

# Expose port 8080 (or whatever port your app uses)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]
