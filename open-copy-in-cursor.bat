@echo off
setlocal EnableDelayedExpansion

REM Get current directory
set "projectDir=%CD%"
for %%* in ("%projectDir%") do set "projectName=%%~nx*"
for %%* in ("%projectDir%") do set "parentDir=%%~dp*"

REM Ask user for new project name
set /p "newName=Enter new project name: "

REM Check if input is empty
if "%newName%"=="" (
    echo Error: Project name cannot be empty.
    exit /b 1
)

REM Set target directory
set "targetDir=%parentDir%%projectName% - %newName%"

REM Check if target exists
if exist "%targetDir%" (
    echo Error: Target directory already exists: %targetDir%
    exit /b 1
)

echo Copying project (excluding logs, target, .git, duck)...

REM Robocopy command
robocopy "%projectDir%" "%targetDir%" /E /XD logs target .git duck >nul

echo.
echo ✅ Project copied to:
echo %targetDir%

echo.
echo 🚀 Opening project in Cursor...
cursor "%targetDir%"

endlocal
pause
