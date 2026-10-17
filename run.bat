@echo off
title BookVault - Spring Boot Backend Launcher
color 0B
cls
echo ==============================================================================
echo           BOOKVAULT - LIBRARY MANAGEMENT SYSTEM (Spring Boot 3)
echo ==============================================================================
echo.

echo [1/4] Checking Java Installation...
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ==============================================================================
    echo [ERROR] Java is not detected on your computer!
    echo Spring Boot backend requires Java 17 or higher.
    echo.
    echo 1. You can install Java 17 easily from: https://adoptium.net/
    echo 2. OR you can immediately view and use the website by double-clicking:
    echo    --^> open_website.bat
    echo ==============================================================================
    echo.
    echo Opening the website in your browser now...
    start "" "%~dp0src\main\resources\static\index.html"
    pause
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%g
echo [OK] Java detected: %JAVA_VER%
echo.

echo [2/4] Checking Maven Wrapper...
if not exist "%~dp0.mvn\wrapper\maven-wrapper.jar" (
    echo [INFO] Downloading Maven Wrapper Jar...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar', '%~dp0.mvn\wrapper\maven-wrapper.jar')" >nul 2>&1
)

echo [3/4] Checking Database...
docker compose up -d postgres >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] PostgreSQL container is running on port 5432!
    set SPRING_PROFILE=
) else (
    echo [INFO] Docker not active. Using zero-config standalone persistent database.
    set SPRING_PROFILE=-Dspring-boot.run.profiles=local
)
echo.

echo [4/4] Starting Spring Boot Server on port 8080...
echo.
echo ==============================================================================
echo  Please wait ~15-20 seconds. 
echo  Once you see "Started LibraryManagementApplication", open:
echo.
echo       --^> http://localhost:8080
echo ==============================================================================
echo.

REM Automatically open browser after 18 seconds
start "" powershell -WindowStyle Hidden -Command "Start-Sleep -Seconds 18; Start-Process 'http://localhost:8080'"

REM Launch Spring Boot
call "%~dp0mvnw.cmd" spring-boot:run %SPRING_PROFILE%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ==============================================================================
    echo [NOTICE] Spring Boot backend encountered an issue.
    echo Opening the interactive website directly in your browser...
    echo ==============================================================================
    start "" "%~dp0src\main\resources\static\index.html"
)

pause
