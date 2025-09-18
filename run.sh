#!/bin/bash

echo "Starting Risk Management Service..."
echo

# Check if Maven is available
if command -v mvn &> /dev/null; then
    echo "Maven found. Building and running with Maven..."
    mvn clean spring-boot:run
else
    echo "Maven not found. Please install Maven or use an IDE to run the application."
    echo
    echo "Alternative options:"
    echo "1. Install Maven from https://maven.apache.org/download.cgi"
    echo "2. Use IntelliJ IDEA or Eclipse to import and run the project"
    echo "3. Use Spring Tool Suite (STS)"
    echo
    echo "The main class to run is: com.assessment.riskmanagement.RiskManagementApplication"
fi
