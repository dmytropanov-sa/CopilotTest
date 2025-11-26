@echo off
REM Builds the backend, starts it in background, waits for it to listen on 8080,
REM extracts the generated dev password from logs and runs a curl auth test.

cd /d "%~dp0"
setlocal enabledelayedexpansion


















































exit /b %HTTP_EXIT%endlocalecho To stop it, find its PID (tasklist /fi "imagename eq java.exe" /v) and stop with: taskkill /PID <pid> /F
necho Script completed. The backend process will keep running in background.type target\auth-test.txtecho === Auth test response (target\auth-test.txt) ===
necho Auth test exit code: %HTTP_EXIT%)  set "HTTP_EXIT=%ERRORLEVEL%"  powershell -NoProfile -Command "$b=[Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('user:%PWD%')); try { (Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/health' -Headers @{ Authorization = 'Basic ' + $b } -UseBasicParsing -TimeoutSec 10).Content | Out-File -FilePath 'target\auth-test.txt' -Encoding utf8; exit 0 } catch { Write-Output '' | Out-File -FilePath 'target\auth-test.txt' -Encoding utf8; exit 1 }") else (  set "HTTP_EXIT=%ERRORLEVEL%"  curl -s -u user:%PWD% http://localhost:8080/api/v1/health -o target\auth-test.txtwhere curl >nul 2>&1
@echo off
REM Builds the backend, starts it in background, waits for it to listen on 8080,
REM extracts the generated dev password from logs and runs a curl auth test.

cd /d "%~dp0"
setlocal enabledelayedexpansion

echo === Building backend (skip tests) ===
mvn -DskipTests package
if %ERRORLEVEL% NEQ 0 (
	echo Maven build failed with %ERRORLEVEL%.
	exit /b %ERRORLEVEL%
)

echo === Starting backend in background (logs -> target\app.log,target\app.err) ===
start "forget-pass-backend" /B java "-Dspring.profiles.active=dev" -jar target\forget-pass-backend-0.1.0-SNAPSHOT.jar > target\app.log 2> target\app.err

echo Waiting for the application to listen on port 8080...
set /a attempts=0
:waitLoop
	timeout /t 1 /nobreak >nul
	set /a attempts+=1
	netstat -ano | findstr /R ":8080[ ]" >nul 2>&1
	if %ERRORLEVEL% EQU 0 goto portOpen
	if %attempts% GEQ 60 (
		echo Timed out waiting for port 8080 after %attempts% seconds.
		echo Check target\app.log and target\app.err for details.
		exit /b 2
	)
	goto waitLoop

:portOpen
echo Port 8080 is open (attempts=%attempts%). Waiting a couple seconds for app readiness...
timeout /t 2 /nobreak >nul

echo Extracting generated security password from target\app.log...
set "PWD="
for /f "usebackq delims=" %%L in (`powershell -NoProfile -Command "Select-String -Path 'target\\app.log' -Pattern 'Using generated security password' -SimpleMatch | Select-Object -Last 1 | ForEach-Object { ($_.Line -split ': ')[1].Trim() }"`) do (
	set "PWD=%%L"
)

if "%PWD%"=="" (
	echo Could not find generated password in target\app.log.
	echo You can check the log manually: target\app.log
	exit /b 3
)

echo Found password: %PWD%

echo Running auth test against /api/v1/health ...

rem Ensure curl is available. If not, PowerShell's Invoke-WebRequest will be used instead.
where curl >nul 2>&1
if %ERRORLEVEL% EQU 0 (
	curl -s -u user:%PWD% http://localhost:8080/api/v1/health -o target\auth-test.txt
	set "HTTP_EXIT=%ERRORLEVEL%"
) else (
	powershell -NoProfile -Command "$b=[Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('user:%PWD%')); try { (Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/health' -Headers @{ Authorization = 'Basic ' + $b } -UseBasicParsing -TimeoutSec 10).Content | Out-File -FilePath 'target\\auth-test.txt' -Encoding utf8; exit 0 } catch { Write-Output '' | Out-File -FilePath 'target\\auth-test.txt' -Encoding utf8; exit 1 }"
	set "HTTP_EXIT=%ERRORLEVEL%"
)

echo Auth test exit code: %HTTP_EXIT%
echo === Auth test response (target\auth-test.txt) ===
type target\auth-test.txt

echo Script completed. The backend process will keep running in background.
echo To stop it, find its PID (tasklist /fi "imagename eq java.exe" /v) and stop with: taskkill /PID <pid> /F
endlocal
exit /b %HTTP_EXIT%