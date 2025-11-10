@echo off
REM Script para iniciar o Resource Server
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
cd /d %~dp0
call mvnw.cmd spring-boot:run
pause

