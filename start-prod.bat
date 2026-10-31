@echo off
REM Production startup script for Appointment Service
REM This script configures JVM memory settings for production environment

REM JVM Memory Configuration
REM -Xms: Initial heap size (512MB)
REM -Xmx: Maximum heap size (1GB) - Adjust based on available memory
REM -XX:MetaspaceSize: Initial Metaspace size (128MB)
REM -XX:MaxMetaspaceSize: Maximum Metaspace size (256MB)
REM -XX:ReservedCodeCacheSize: Code cache size (240MB)

REM JVM Performance Tuning
REM -XX:+UseG1GC: Use G1 Garbage Collector (recommended for production)
REM -XX:MaxGCPauseMillis: Target max GC pause time (200ms)
REM -XX:G1HeapRegionSize: G1 heap region size (16MB)
REM -XX:+HeapDumpOnOutOfMemoryError: Generate heap dump on OOM
REM -XX:HeapDumpPath: Path for heap dump files

REM JVM Crash Handling
REM -XX:+ExitOnOutOfMemoryError: Exit JVM on OOM instead of crashing
REM -XX:ErrorFile: Path for error log file

set JAVA_OPTS=-Xms512m -Xmx1024m ^
-XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m ^
-XX:ReservedCodeCacheSize=240m ^
-XX:+UseG1GC ^
-XX:MaxGCPauseMillis=200 ^
-XX:G1HeapRegionSize=16m ^
-XX:+HeapDumpOnOutOfMemoryError ^
-XX:HeapDumpPath=./logs/heapdump.hprof ^
-XX:+ExitOnOutOfMemoryError ^
-XX:ErrorFile=./logs/hs_err_pid%%p.log ^
-Dspring.profiles.active=prod

REM Create logs directory if not exists
if not exist "logs" mkdir logs

REM Run the application
java %JAVA_OPTS% -jar target/appointment_service-0.0.1-SNAPSHOT.jar
