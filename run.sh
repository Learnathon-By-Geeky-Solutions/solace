#!/bin/bash

# Step 1: Check and Apply Spotless Formatting
echo "Checking if code formatting is necessary..."
mvn spotless:check
if [ $? -ne 0 ]; then
  echo "Code formatting issues found. Applying formatting with Spotless..."
  mvn spotless:apply
  if [ $? -ne 0 ]; then
    echo "Spotless formatting failed. Please check your code."
    exit 1
  fi
  echo "Code formatting completed successfully."
else
  echo "Code formatting is already up to date."
fi

# Step 2: Build the application
echo "Building the application..."
mvn clean install
if [ $? -ne 0 ]; then
  echo "Build failed. Fix the errors and try again."
  exit 1
fi
echo "Build completed successfully."

# Step 3: Run the application
JAR_FILE=$(ls target/*.jar | head -n 1)
if [ -z "$JAR_FILE" ]; then
  echo "No JAR file found in the target directory. Build might have failed."
  exit 1
fi

echo "Running the application from $JAR_FILE..."
java -jar "$JAR_FILE"
