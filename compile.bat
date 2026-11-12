@echo off
title EcoNest – Build & Compile System
cd /d "%~dp0"

echo ===============================================================
echo  Compiling EcoNest Application Sources...
echo ===============================================================

if not exist "target\classes" mkdir "target\classes"

dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d target\classes @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] EcoNest successfully compiled into target\classes!
) else (
    echo.
    echo [ERROR] Compilation failed.
)
pause
