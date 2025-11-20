@echo off
echo ===============================
echo Starting Kafka + Auth + User Services
echo ===============================

REM --- 1. Start Kafka broker ---
start powershell -NoExit -Command "cd 'D:\kafka_2.13-4.0.1\bin\windows'; .\kafka-server-start.bat ..\..\config\kraft-server.properties"

REM --- 2. Start auth-service ---
start powershell -NoExit -Command "cd 'D:\nam_4\OJT\user-access-management\auth-service'; mvn spring-boot:run"

REM --- 3. Start user-service ---
start powershell -NoExit -Command "cd 'D:\nam_4\OJT\user-access-management\user-service'; mvn spring-boot:run"

echo ===============================
echo All services started.
echo ===============================
pause
