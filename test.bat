@echo off
title EcoNest – Automated Test Suite
cd /d "%~dp0"

echo Running System Integration & OOP Verification Test Suite...
java -cp "lib/*;target/classes" com.econest.test.SystemIntegrationTest
pause
