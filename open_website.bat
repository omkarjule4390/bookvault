@echo off
title Opening BookVault Library Website...
echo ==============================================================================
echo           Opening BookVault Library Management System in Browser
echo ==============================================================================
echo.
echo Opening the interactive web application...
start "" "%~dp0src\main\resources\static\index.html"
echo.
echo [SUCCESS] The website is now open in your browser!
exit
