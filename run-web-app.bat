@echo off
echo ===================================================
echo Banking System Web Application Runner
echo ===================================================

echo This script will open the web application in your browser.
echo The application will be accessible at http://localhost:8080

echo Starting the application...
echo (This may take a moment to initialize)

cd /d %~dp0
java -jar banking-system.jar

pause
