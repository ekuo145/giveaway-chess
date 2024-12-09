# Use the official OpenJDK image (Java 17 as base image)
FROM openjdk:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy application files (your built JAR) into the container
COPY target/giveaway-chess-1.0-SNAPSHOT.jar /app/giveaway-chess.jar

# Expose the application port (set this to the port your app uses, e.g., 8080)
EXPOSE 8080

# Define the command to run the application
CMD ["java", "-jar", "giveaway-chess.jar"]
