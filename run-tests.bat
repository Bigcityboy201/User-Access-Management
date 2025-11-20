@echo off
REM Script to run tests for User Access Management project

echo ========================================
echo User Access Management - Test Runner
echo ========================================
echo.

if "%1"=="" (
    echo Usage:
    echo   run-tests.bat [option]
    echo.
    echo Options:
    echo   all          - Run all tests (default)
    echo   core         - Run core module tests only
    echo   system       - Run system tests only
    echo   auth         - Run auth-service tests only
    echo   user         - Run user-service tests only
    echo   unit         - Run unit tests only (skip integration)
    echo.
    echo Example:
    echo   run-tests.bat core
    echo.
    set OPTION=all
) else (
    set OPTION=%1
)

echo Running tests: %OPTION%
echo.

if "%OPTION%"=="all" (
    echo Running all tests...
    mvn clean test
) else if "%OPTION%"=="core" (
    echo Running core module tests...
    mvn clean test -pl core
) else if "%OPTION%"=="system" (
    echo Running system tests...
    mvn clean test -pl system-tests
) else if "%OPTION%"=="auth" (
    echo Running auth-service tests...
    mvn clean test -pl auth-service
) else if "%OPTION%"=="user" (
    echo Running user-service tests...
    mvn clean test -pl user-service
) else if "%OPTION%"=="unit" (
    echo Running unit tests only...
    mvn clean test -Dtest=*Test -Dtest=!*IntegrationTest
) else (
    echo Unknown option: %OPTION%
    echo Use: all, core, system, auth, user, or unit
    exit /b 1
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Tests completed successfully!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Tests failed with errors!
    echo ========================================
    exit /b 1
)

