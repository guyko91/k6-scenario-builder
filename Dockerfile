# Use an official Gradle image with JDK 17
FROM gradle:jdk17-focal

# Set the working directory
WORKDIR /home/gradle/project

# Copy the entire project context to the container
COPY . .

# Grant execute permissions to the gradlew script if it exists in the future
# Even though it doesn't exist now, this is good practice
RUN if [ -f "gradlew" ]; then chmod +x gradlew; fi

# Define the default command to build the project
# Using 'gradle' directly from the image
CMD ["gradle", "build"]
