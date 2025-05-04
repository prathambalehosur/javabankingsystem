@echo off
echo Starting Banking System Application...

rem Set Java classpath
set CLASSPATH=.;target\classes;target\dependency\*

rem Check if compiled classes exist
if not exist "target\classes\com\bankingsystem\BankingApplication.class" (
  echo Compiling application...
  if not exist "target\classes" mkdir target\classes
  javac -d target\classes -cp "%CLASSPATH%" src\main\java\com\bankingsystem\BankingApplication.java
)

rem Run the application
echo Running Banking System Application...
java -cp "%CLASSPATH%" com.bankingsystem.BankingApplication

echo Application stopped.
pause
