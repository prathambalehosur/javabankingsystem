@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8082"
