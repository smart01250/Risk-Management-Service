@echo off
echo Starting Risk Management Service on port 8081...
echo.

REM Set JAVA_HOME if not already set
if not defined JAVA_HOME (
    set JAVA_HOME=C:\Program Files\Java\jdk-21
)

REM Kill any existing Java processes that might be holding ports
echo Checking for existing Java processes...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

REM Check if Maven wrapper is available
if exist mvnw.cmd (
    echo Maven wrapper found. Building and running with Maven wrapper...
    mvnw.cmd spring-boot:run
) else if exist mvn.cmd (
    echo Maven found. Building and running with Maven...
    mvn clean spring-boot:run
) else (
    echo Maven not found. Please install Maven or use an IDE like IntelliJ IDEA or Eclipse to run the application.
    echo.
    echo Alternative options:
    echo 1. Install Maven from https://maven.apache.org/download.cgi
    echo 2. Use IntelliJ IDEA or Eclipse to import and run the project
    echo 3. Use Spring Tool Suite (STS)
    echo.
    echo The main class to run is: com.assessment.riskmanagement.RiskManagementApplication
    pause
)
