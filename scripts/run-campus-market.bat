@echo off
cd /d "%~dp0.."
set "JAVA_BIN=C:\Program Files\Common Files\Oracle\Java\javapath\javaw.exe"
if not exist "%JAVA_BIN%" set "JAVA_BIN=java"
"%JAVA_BIN%" -jar "target\campus-market-0.0.1-SNAPSHOT.jar"
