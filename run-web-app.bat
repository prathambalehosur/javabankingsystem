@echo off
echo ===================================================
echo Banking System Web Application Runner
echo ===================================================

echo This script will open the web application in your browser.
echo The application will be accessible at http://localhost:8081

echo Stopping any running Java processes...
taskkill /F /IM java.exe >nul 2>&1

echo Starting the application...
echo (This may take a moment to initialize)

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

start "" "http://localhost:8081"
call mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081"

cd /d %~dp0
java -jar banking-system.jar

pause
