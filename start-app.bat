@echo off
set MAVEN_OPTS=-Xmx512m -XX:MaxPermSize=256m
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8082"
