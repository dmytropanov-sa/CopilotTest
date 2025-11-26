@echo off
REM Run backend in dev mode: builds (skips tests) and runs jar with dev profile.
cd /d "%~dp0"
echo Building backend (skip tests)...
mvn -DskipTests package
if %ERRORLEVEL% NEQ 0 (
  echo Maven build failed with %ERRORLEVEL%.
  exit /b %ERRORLEVEL%
)
echo Starting backend in foreground with profile 'dev'...
java "-Dspring.profiles.active=dev" -jar target\forget-pass-backend-0.1.0-SNAPSHOT.jar

REM Press Ctrl+C to stop the application.
