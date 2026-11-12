@echo off
title EcoNest – Smart Home & Garden Retail Management System
cd /d "%~dp0"

echo ===============================================================
echo  EcoNest – Smart Home & Garden Retail Management System
echo  University OOP Group Project (HDIT 2026)
echo ===============================================================
echo Starting EcoNest Desktop Application...

java -cp "lib/*;target/classes" com.econest.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error.
    pause
)
