@echo off
echo ===================================================
echo Banking System Web Application Runner
echo ===================================================

echo This script will start the Spring Boot banking web application
echo The application will be accessible at http://localhost:8080

echo Starting the application...
echo (This may take a moment to initialize)

cd /d %~dp0
C:\Users\prath\apache-maven-3.9.6\bin\mvn spring-boot:run

pause
