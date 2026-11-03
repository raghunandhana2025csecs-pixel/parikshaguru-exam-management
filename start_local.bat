@echo off
echo ==============================================
echo ParikshaGuru Local Development Setup
echo ==============================================
echo.
echo This script helps set up ParikshaGuru locally.
echo.

REM Check prerequisites
echo Checking prerequisites...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Please install Java 21+.
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven not found. Please install Maven.
    pause
    exit /b 1
)

where node >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Node.js not found. Please install Node.js 18+.
    pause
    exit /b 1
)

echo ✓ All prerequisites found
echo.

REM Setup section
echo SETUP OPTIONS:
echo 1. Start with H2 (testing only - no Aiven required)
echo 2. Start with Aiven MySQL (production database)
echo 3. Install dependencies only
echo 4. Exit
echo.
set /p choice="Choose option (1-4): "

if "%choice%"=="1" goto :h2_setup
if "%choice%"=="2" goto :aiven_setup
if "%choice%"=="3" goto :install_deps
if "%choice%"=="4" goto :exit

echo Invalid choice
goto :exit

:aiven_setup
echo.
echo ==============================================
echo AIVEN MYSQL SETUP
echo ==============================================
echo.
echo Please ensure you have:
echo 1. Aiven MySQL service credentials
echo 2. Created .env file with Aiven details
echo.
echo If you haven't configured .env file yet:
echo 1. Navigate to: parikshaguru-backend\parikshaguru-backend
echo 2. Copy .env.example to .env
echo 3. Edit .env with your Aiven credentials
echo.
echo Starting backend with Aiven profile...
echo.

REM Start backend in new window
start "ParikshaGuru Backend (Aiven)" cmd /k "cd /d %~dp0\parikshaguru-backend\parikshaguru-backend && echo Starting backend... && mvn clean compile && mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=aiven"

echo Backend starting... Waiting 10 seconds...
timeout /t 10 >nul

echo.
echo Starting frontend...
echo.

REM Start frontend in new window
start "ParikshaGuru Frontend" cmd /k "cd /d %~dp0\ParikshaGuru Frontend && echo Starting frontend... && npm start"

echo.
echo ==============================================
echo APPLICATION STARTED
echo ==============================================
echo Backend: http://localhost:8080/api
echo Frontend: http://localhost:3000/sign_in/code.html
echo.
echo Test the application by:
echo 1. Opening the frontend URL
echo 2. Creating a user account
echo 3. Testing the exam flow
echo.
pause
goto :exit

:h2_setup
echo.
echo ==============================================
echo H2 IN-MEMORY DATABASE SETUP
echo ==============================================
echo.
echo This uses H2 in-memory database for testing only.
echo Data will be lost when the server stops.
echo.
echo Starting backend with H2 profile...
echo.

REM Start backend in new window
start "ParikshaGuru Backend (H2)" cmd /k "cd /d %~dp0\parikshaguru-backend\parikshaguru-backend && echo Starting backend... && mvn clean compile && mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=h2"

echo Backend starting... Waiting 10 seconds...
timeout /t 10 >nul

echo.
echo Starting frontend...
echo.

REM Start frontend in new window
start "ParikshaGuru Frontend" cmd /k "cd /d %~dp0\ParikshaGuru Frontend && echo Starting frontend... && npm start"

echo.
echo ==============================================
echo APPLICATION STARTED
echo ==============================================
echo Backend: http://localhost:8080/api
echo Frontend: http://localhost:3000/sign_in/code.html
echo.
echo Demo credentials:
echo - Student: rohan.verma@university.ac.in / Student@123
echo - Faculty: nair@university.ac.in / Faculty@123
echo - Admin: admin@test.local / Admin@123
echo.
pause
goto :exit

:install_deps
echo.
echo ==============================================
echo INSTALLING DEPENDENCIES
echo ==============================================
echo.
echo Installing frontend dependencies...
cd /d %~dp0\ParikshaGuru Frontend
if exist node_modules (
    echo Frontend dependencies already installed.
) else (
    npm install
    if %errorlevel% neq 0 (
        echo ERROR: Failed to install frontend dependencies.
        pause
        exit /b 1
    )
)

echo.
echo Building backend...
cd /d %~dp0\parikshaguru-backend\parikshaguru-backend
mvn clean compile
if %errorlevel% neq 0 (
    echo ERROR: Failed to build backend.
    pause
    exit /b 1
)

echo.
echo ✓ All dependencies installed successfully.
echo.
pause
goto :exit

:exit
echo.
echo ==============================================
echo SETUP COMPLETE
echo ==============================================
echo For detailed instructions, see RUN_LOCAL.md
echo.
pause