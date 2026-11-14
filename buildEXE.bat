@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  MC-Lonia - Build Executable (Build.exe)
echo ========================================
echo.

REM Verificar que Gradle wrapper existe
if not exist "gradlew.bat" (
    echo ERROR: No se encontro gradlew.bat
    pause
    exit /b 1
)

REM Paso 1: Compilar e instalar con Gradle
echo [1/3] Compilando proyecto con Gradle...
call gradlew.bat clean build installDist
if errorlevel 1 (
    echo ERROR: Fallo la compilacion de Gradle
    pause
    exit /b 1
)
echo.

REM Paso 2: Limpiar carpeta EXE previa
echo [2/3] Limpiando carpeta EXE anterior...
if exist "EXE" rmdir /s /q "EXE"
echo.

REM Paso 3: Generar Build.exe con jpackage
echo [3/3] Generando Build.exe con jpackage...
jpackage --input build/install/mclonia/lib --main-jar mclonia-1.0-SNAPSHOT.jar --main-class game.Main --name Build --dest EXE --type app-image
if errorlevel 1 (
    echo ERROR: Fallo la generacion del EXE
    pause
    exit /b 1
)
echo.

echo ========================================
echo  Build.exe generado exitosamente
echo  Ubicacion: EXE\Build\Build.exe
echo ========================================
pause
