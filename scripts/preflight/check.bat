@echo off
chcp 65001 >nul
title NEXUS Preflight Deployment Check
echo.
echo ============================================================
echo   NEXUS Preflight Deployment Check
echo ============================================================
echo.

set "PASS=0"
set "FAIL=0"
set "WARN=0"

cd /d "%~dp0.."

:: ---- 1. local.properties ----
echo [1/7] Checking local.properties...
if not exist local.properties (
    echo   [FAIL] local.properties not found
    set /a FAIL+=1
    goto :check_keystore
)
echo   [PASS] local.properties found
set /a PASS+=1

:: Read property helper
:getprop
for /f "tokens=1,* delims==" %%a in ('findstr /b "%~1=" local.properties 2^>nul') do (
    set "VAL=%%b"
    goto :getprop_done
)
set "VAL="
:getprop_done
exit /b 0

call :getprop sdk.dir
if defined VAL (
    if exist "%VAL%" (
        echo   [PASS] sdk.dir: %VAL%
        set /a PASS+=1
    ) else (
        echo   [WARN] sdk.dir path not found: %VAL%
        set /a WARN+=1
    )
) else (
    echo   [FAIL] sdk.dir not set
    set /a FAIL+=1
)

call :getprop OPENROUTER_API_KEY
if not defined VAL echo   [FAIL] OPENROUTER_API_KEY missing ^(run scripts\setup_secrets.bat^) & set /a FAIL+=1 & goto :skip_keys
if "%VAL%"==placeholder echo   [FAIL] OPENROUTER_API_KEY is placeholder & set /a FAIL+=1 & goto :skip_keys
echo   [PASS] OPENROUTER_API_KEY set
set /a PASS+=1

call :getprop GITHUB_CLIENT_ID
if not defined VAL echo   [FAIL] GITHUB_CLIENT_ID missing & set /a FAIL+=1 & goto :skip_keys
if "%VAL%"==placeholder echo   [FAIL] GITHUB_CLIENT_ID is placeholder & set /a FAIL+=1 & goto :skip_keys
echo   [PASS] GITHUB_CLIENT_ID set
set /a PASS+=1

call :getprop GITHUB_CLIENT_SECRET
if not defined VAL echo   [FAIL] GITHUB_CLIENT_SECRET missing & set /a FAIL+=1 & goto :skip_keys
if "%VAL%"==placeholder echo   [FAIL] GITHUB_CLIENT_SECRET is placeholder & set /a FAIL+=1 & goto :skip_keys
echo   [PASS] GITHUB_CLIENT_SECRET set
set /a PASS+=1

call :getprop GOOGLE_WEB_CLIENT_ID
if not defined VAL echo   [FAIL] GOOGLE_WEB_CLIENT_ID missing & set /a FAIL+=1 & goto :skip_keys
if "%VAL%"==placeholder echo   [FAIL] GOOGLE_WEB_CLIENT_ID is placeholder & set /a FAIL+=1 & goto :skip_keys
echo   [PASS] GOOGLE_WEB_CLIENT_ID set
set /a PASS+=1

:skip_keys

:: ---- 2. Keystore ----
echo.
echo [2/7] Checking release keystore...
call :getprop KEYSTORE_PATH
call :getprop KEY_ALIAS
call :getprop STORE_PASSWORD
call :getprop KEY_PASSWORD

if not defined VAL (
    echo   [FAIL] Keystore not configured. Run: scripts\create_keystore.bat
    set /a FAIL+=1
) else (
    if exist "%VAL%" (
        echo   [PASS] Keystore found: %VAL%
        set /a PASS+=1
    ) else (
        echo   [FAIL] Keystore file missing at: %VAL%
        set /a FAIL+=1
    )
)

:: ---- 3. google-services.json ----
echo.
echo [3/7] Checking Firebase config...
if exist "app\google-services.json" (
    echo   [PASS] app\google-services.json found
    set /a PASS+=1
    findstr "nexus-1a61a" "app\google-services.json" >nul 2>&1
    if not errorlevel 1 (
        echo   [PASS] Firebase project_id: nexus-1a61a
        set /a PASS+=1
    ) else (
        echo   [WARN] project_id not found in google-services.json
        set /a WARN+=1
    )
    findstr "com.nexus.app" "app\google-services.json" >nul 2>&1
    if not errorlevel 1 (
        echo   [PASS] Android package: com.nexus.app
        set /a PASS+=1
    ) else (
        echo   [WARN] package_name not com.nexus.app
        set /a WARN+=1
    )
) else (
    echo   [FAIL] app\google-services.json not found
    set /a FAIL+=1
)

:: ---- 4. Build system ----
echo.
echo [4/7] Checking build system...
if exist gradlew (
    echo   [PASS] gradlew found
    set /a PASS+=1
) else (
    echo   [FAIL] gradlew not found
    set /a FAIL+=1
)
findstr "nexus-1a61a" backend\.firebaserc >nul 2>&1
if errorlevel 1 (
    findstr "nexus-app-prod" backend\.firebaserc >nul 2>&1
    if errorlevel 1 (
        echo   [WARN] backend/.firebaserc has no default project
        set /a WARN+=1
    ) else (
        echo   [PASS] backend/.firebaserc: nexus-app-prod
        set /a PASS+=1
    )
) else (
    echo   [PASS] backend/.firebaserc: nexus-1a61a
    set /a PASS+=1
)
if exist backend\functions\package.json (
    echo   [PASS] backend/functions/package.json found
    set /a PASS+=1
) else (
    echo   [FAIL] backend/functions/package.json missing
    set /a FAIL+=1
)
if exist backend\seed\opportunities.json (
    echo   [PASS] Seed data: opportunities.json found
    set /a PASS+=1
) else (
    echo   [FAIL] Seed data: opportunities.json missing
    set /a FAIL+=1
)

:: ---- 5. Gradle config ----
echo.
echo [5/7] Checking Gradle config...
if exist gradle\libs.versions.toml (
    echo   [PASS] Version catalog found
    set /a PASS+=1
) else (
    echo   [FAIL] Version catalog missing
    set /a FAIL+=1
)
if exist app\build.gradle.kts (
    echo   [PASS] app/build.gradle.kts found
    set /a PASS+=1
) else (
    echo   [FAIL] app/build.gradle.kts missing
    set /a FAIL+=1
)
findstr "com.nexus.app" app\build.gradle.kts >nul 2>&1
if not errorlevel 1 (
    echo   [PASS] applicationId = com.nexus.app
    set /a PASS+=1
) else (
    echo   [WARN] applicationId may be wrong
    set /a WARN+=1
)

:: ---- 6. Native modules ----
echo.
echo [6/7] Checking native source modules...
if exist core\common\src\main\kotlin\com\nexus\common\Constants.kt (
    echo   [PASS] core:common
    set /a PASS+=1
) else (
    echo   [FAIL] core:common missing
    set /a FAIL+=1
)
if exist core\data\src\main\kotlin\com\nexus\data\di\RepositoryModule.kt (
    echo   [PASS] core:data
    set /a PASS+=1
) else (
    echo   [FAIL] core:data missing
    set /a FAIL+=1
)
if exist core\network\src\main\kotlin\com\nexus\network\di\NetworkModule.kt (
    echo   [PASS] core:network
    set /a PASS+=1
) else (
    echo   [FAIL] core:network missing
    set /a FAIL+=1
)
if exist core\database\src\main\kotlin\com\nexus\database\NexusDatabase.kt (
    echo   [PASS] core:database
    set /a PASS+=1
) else (
    echo   [FAIL] core:database missing
    set /a FAIL+=1
)
if exist core\sync\src\main\kotlin\com\nexus\sync\SyncScheduler.kt (
    echo   [PASS] core:sync
    set /a PASS+=1
) else (
    echo   [FAIL] core:sync missing
    set /a FAIL+=1
)
if exist core\notifications\src\main\kotlin\com\nexus\notifications\NotificationHelper.kt (
    echo   [PASS] core:notifications
    set /a PASS+=1
) else (
    echo   [FAIL] core:notifications missing
    set /a FAIL+=1
)

:: ---- 7. Feature modules ----
echo.
echo [7/7] Checking feature modules...
set "FEATURES=onboarding home internships certifications productivity profile achievements settings auth explore ai growth goals notifications portfolio community applications paywall search careermirror placementforecast cohortintelligence interviewlog tribe"
set "FEAT_PASS=0"
for %%f in (%FEATURES%) do (
    if exist "feature\%%f\build.gradle.kts" (
        set /a FEAT_PASS+=1
    ) else (
        echo   [FAIL] feature:%%f missing build.gradle.kts
        set /a FAIL+=1
    )
)
echo   [PASS] %FEAT_PASS%/24 feature modules found
set /a PASS+=1

:: ---- Summary ----
echo.
echo ============================================================
echo   SUMMARY
echo ============================================================
echo   PASS: %PASS%
echo   WARN: %WARN%
echo   FAIL: %FAIL%
echo.

if %FAIL% GTR 0 (
    echo [BLOCKERS] Fix %FAIL% FAIL item(s) above before building.
    echo.
    echo Quick fixes:
    echo   - Missing keys^: scripts\setup_secrets.bat
    echo   - Missing keystore^: scripts\create_keystore.bat
    echo   - Missing google-services.json^: Download from Firebase Console
    pause
    exit /b 1
)

if %WARN% GTR 0 (
    echo [WARNINGS] %WARN% warning(s) found. App may build but review items above.
    echo.
    echo To proceed:
    echo   1. scripts\create_keystore.bat
    echo   2. cd backend\functions ^& npm install ^& firebase deploy --only functions
    echo   3. cd backend\seed ^& npm install ^& node seed_firestore.sh
    echo   4. gradlew :app:validateReleaseBuild
    echo   5. gradlew :app:bundleRelease
    pause
    exit /b 0
)

echo [ALL PASS] All checks passed! Ready to build.
echo.
echo Next steps:
echo   1. scripts\create_keystore.bat       ^(if not done yet^)
echo   2. gradlew :app:validateReleaseBuild
echo   3. gradlew :app:bundleRelease
echo.
pause
exit /b 0
