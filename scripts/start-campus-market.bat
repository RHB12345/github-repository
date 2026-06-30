@echo off
setlocal
cd /d "%~dp0.."
set "MAVEN_HOME=%CD%\tools\apache-maven-3.9.11"
set "PATH=%MAVEN_HOME%\bin;%PATH%"
set "APP_URL=http://localhost:8080"
set "JAR_PATH=%CD%\target\campus-market-0.0.1-SNAPSHOT.jar"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Maven was not found at %MAVEN_HOME%.
  echo Please run the environment setup again.
  pause
  exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$listener = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Where-Object { $_.State -eq 'Listen' }; if ($listener) { Start-Process '%APP_URL%'; exit 0 }; exit 1"
if %ERRORLEVEL% EQU 0 (
  echo Campus market is already running. Opening %APP_URL% ...
  exit /b 0
)

start "" powershell.exe -NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File "%CD%\scripts\open-browser-when-ready.ps1" -Url "%APP_URL%"

echo Starting campus market at %APP_URL%
echo Demo user: 2023123401 / 123456
echo Admin user: 2023000001 / 123456

if not exist "%JAR_PATH%" (
  echo Packaged jar was not found. Building project first...
  mvn -DskipTests package
  if errorlevel 1 (
    echo Build failed. Please check the output above.
    pause
    exit /b 1
  )
)

java -jar "%JAR_PATH%"
pause
