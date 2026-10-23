@echo off
chcp 65001 >nul
title NEXUS — Create Release Keystore
echo.
echo ============================================================
echo   NEXUS Release Keystore Generator
echo ============================================================
echo.

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
set "OUTPUT=%PROJECT_DIR%nexus-release.keystore"

if exist "%OUTPUT%" (
    echo Keystore already exists at:
    echo   %OUTPUT%
    set /p OVERWRITE="Overwrite? [y/N]: "
    if /i not "%OVERWRITE%"=="y" (
        echo Aborted.
        pause
        exit /b 0
    )
)

echo Output: %OUTPUT%
echo.

set /p KEY_ALIAS="Key alias [nexus-key]: "
if "%KEY_ALIAS%"=="" set "KEY_ALIAS=nexus-key"

set /p VALIDITY="Validity (years) [25]: "
if "%VALIDITY%"=="" set "VALIDITY=25"

set /p DNAME="Distinguished Name [CN=NEXUS, OU=Engineering, O=NEXUS, L=Bangalore, ST=KA, C=IN]: "
if "%DNAME%"=="" set "DNAME=CN=NEXUS, OU=Engineering, O=NEXUS, L=Bangalore, ST=KA, C=IN"

echo.
echo Generating keystore...
keytool -genkey -v ^
  -keystore "%OUTPUT%" ^
  -alias "%KEY_ALIAS%" ^
  -keyalg RSA ^
  -keysize 4096 ^
  -validity "%VALIDITY%" ^
  -dname "%DNAME%"

if errorlevel 1 (
    echo.
    echo [ERROR] Keystore generation failed. Is Java JDK installed?
    echo       Install from: https://adoptium.net/
    pause
    exit /b 1
)

echo.
echo ============================================================
echo   Keystore Created Successfully!
echo ============================================================
echo.
echo IMPORTANT: Save these passwords — you will need them for every update.
echo.
set /p STORE_PASS="Enter keystore password (STORE_PASSWORD): "
set /p KEY_PASS="Enter key password (KEY_PASSWORD, press Enter to use same as keystore): "
if "%KEY_PASS%"=="" set "KEY_PASS=%STORE_PASS%"

echo.
echo Updating local.properties...
set "LP=%PROJECT_DIR%local.properties"

(
    echo ## This file must *NOT* be checked into Version Control Systems,
    echo # as it contains information specific to your local configuration.
    echo #
    echo # Location of the SDK. This is only used by Gradle.
    echo # For customization when using a Version Control System, please read the
    echo # header note.
    findstr /b "sdk.dir=" "%LP%" 2>nul
    echo.
    echo KEYSTORE_PATH=%OUTPUT:\=\=%
    echo KEY_ALIAS=%KEY_ALIAS%
    echo STORE_PASSWORD=%STORE_PASS%
    echo KEY_PASSWORD=%KEY_PASS%
) > "%LP%.tmp"

move /Y "%LP%.tmp" "%LP%" >nul

echo.
echo ============================================================
echo   local.properties updated with keystore settings
echo ============================================================
echo.
echo Your keystore:       %OUTPUT%
echo Key alias:           %KEY_ALIAS%
echo Store password:      %STORE_PASS%
echo Key password:        %KEY_PASS%
echo.
echo SAVE THESE PASSWORDS SOMEWHERE SAFE!
echo If you lose them, you can NEVER update your app on Play Store.
echo.
pause
