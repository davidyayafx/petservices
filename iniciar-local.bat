@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

set "PUERTO=%~1"
if "%PUERTO%"=="" set "PUERTO=8081"

REM Maven portatil incluido en el repositorio; si no existe, usa el del PATH.
set "MVN=mvn"
if exist "..\herramientas\apache-maven\bin\mvn.cmd" set "MVN=..\herramientas\apache-maven\bin\mvn.cmd"

echo.
echo Iniciando PetServices en http://localhost:%PUERTO%/petservices
echo Presiona Ctrl+C para detener el servidor.
echo.

call "%MVN%" tomcat7:run -Dapp.port=%PUERTO%