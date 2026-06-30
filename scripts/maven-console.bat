@echo off
setlocal
cd /d "%~dp0.."
set "MAVEN_HOME=%CD%\tools\apache-maven-3.9.11"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Maven was not found at %MAVEN_HOME%.
  pause
  exit /b 1
)

echo Local Maven is ready.
echo Project: %CD%
echo.
cmd /k
