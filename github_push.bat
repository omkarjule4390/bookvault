@echo off
title Push BookVault to GitHub
echo ==============================================================
echo   Push BookVault to GitHub
echo   Pahile github.com var navin EMPTY repo bana (README/license nako).
echo ==============================================================
echo.

where git >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Git install nahi. Install kar: https://git-scm.com/downloads
    pause
    exit /b 1
)

if not exist "%~dp0src\main\java" (
    echo [ERROR] src\main\java folder nahi sapadla. Tujha src folder ya folder madhe copy kar.
    pause
    exit /b 1
)

set /p REPO_URL=GitHub repo URL paste kar (e.g. https://github.com/username/bookvault.git): 

cd /d "%~dp0"
git init
git add .
git commit -m "BookVault: initial commit"
git branch -M main
git remote remove origin >nul 2>&1
git remote add origin %REPO_URL%
git push -u origin main

echo.
echo Done. GitHub repo refresh karun bagh.
pause
