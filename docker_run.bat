@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

cls
echo ╔═══════════════════════════════════════╗
echo ║           UML Generator               ║
echo ╚═══════════════════════════════════════╝
echo.
echo Iniciando el proceso de despliegue...
echo.

REM Verificar si Docker está instalado
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [⨯] Docker no está instalado o no está en el PATH.
    echo    Para solucionar esto:
    echo    1. Instala Docker Desktop desde https://www.docker.com/products/docker-desktop
    echo    2. Asegúrate de que Docker Desktop está en ejecución
    pause
    exit /b 1
)

REM Verificar si Docker está en ejecución
docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [⨯] Docker no está en ejecución.
    echo    Para solucionar esto:
    echo    1. Abre Docker Desktop
    echo    2. Espera a que el icono de la ballena esté verde
    echo    3. Vuelve a ejecutar este script
    pause
    exit /b 1
)

REM Verificar si el contenedor ya existe
docker-compose ps -q >nul 2>nul
if %errorlevel% equ 0 (
    echo [ℹ] Se ha detectado una versión anterior.
    set /p REBUILD="¿Deseas reconstruir la aplicación? (S/N): "
    if /i "!REBUILD!" neq "S" (
        echo [ℹ] Usando versión existente...
        docker-compose start >nul 2>nul
        goto :start_browser
    )
)

echo [1/6] Preparando el entorno...
if not exist uploads mkdir uploads >nul 2>nul
if not exist uml_output mkdir uml_output >nul 2>nul
if exist uploads\* del /Q /S uploads\* >nul 2>nul
if exist uml_output\* del /Q /S uml_output\* >nul 2>nul

echo [2/6] Deteniendo versiones anteriores...
docker-compose down >nul 2>nul

echo [3/6] Limpiando espacio temporal...
docker system prune -f >nul 2>nul

echo [4/6] Preparando la aplicación...
echo    ⚙ Esto puede tardar unos minutos...
echo.

REM Limpiar archivos temporales y procesos previos
taskkill /F /IM powershell.exe >nul 2>nul
del "%temp%\build.done" 2>nul
del "%temp%\build.cancel" 2>nul
del "%temp%\docker_build.log" 2>nul
timeout /t 1 /nobreak >nul

REM Iniciar build en segundo plano
start /b cmd /c "docker-compose build --no-cache >%temp%\docker_build.log 2>&1 && echo done >%temp%\build.done"

REM Mostrar animación mientras se construye
start /b powershell -NoProfile -ExecutionPolicy Bypass -File "loading_animation.ps1" "%temp%\build.done" "%processid%"

:wait_for_build
if not exist "%temp%\build.done" (
    REM Verificar si se presionó Ctrl+C
    if exist "%temp%\build.cancel" (
        echo.
        echo [⨯] Construcción cancelada por el usuario
        docker-compose down >nul 2>nul
        del "%temp%\build.done" 2>nul
        del "%temp%\docker_build.log" 2>nul
        del "%temp%\build.cancel" 2>nul
        taskkill /F /IM powershell.exe >nul 2>nul
        exit /b 1
    )
    timeout /t 1 /nobreak >nul
    goto :wait_for_build
)

taskkill /F /IM powershell.exe >nul 2>nul
echo.
echo    [✓] Construcción completada
echo.

REM Verificar si hubo errores reales
findstr /i /C:"BUILD FAILED" /C:"ERROR:" /C:"No such file" "%temp%\docker_build.log" >nul
if %errorlevel% equ 0 (
    echo    [⨯] Error al construir la aplicación
    echo.
    echo Detalles del error:
    type "%temp%\docker_build.log"
    del "%temp%\docker_build.log" 2>nul
    pause
    exit /b 1
)

REM Verificar si el build fue exitoso
findstr /i "BUILD SUCCESSFUL" "%temp%\docker_build.log" >nul
if %errorlevel% neq 0 (
    echo    [⨯] No se pudo verificar si la construcción fue exitosa
    echo.
    echo Detalles del build:
    type "%temp%\docker_build.log"
    del "%temp%\docker_build.log" 2>nul
    pause
    exit /b 1
)

del "%temp%\build.done" 2>nul
del "%temp%\docker_build.log" 2>nul

echo [5/6] Iniciando la aplicación...
docker-compose up -d >nul 2>nul
if %errorlevel% neq 0 (
    echo [⨯] No se pudo iniciar la aplicación.
    echo    Para solucionar esto:
    echo    1. Asegúrate de que el puerto 8080 no está en uso
    echo    2. Intenta reiniciar Docker Desktop
    pause
    exit /b 1
)

:start_browser
echo [6/6] Finalizando la configuración...
echo    ⚙ Esperando a que la aplicación esté lista...

REM Verificar la conectividad con reintentos
set /a intentos=0
:retry_connection
powershell -Command "(New-Object Net.WebClient).DownloadString('http://localhost:8080')" >nul 2>nul
if %errorlevel% neq 0 (
    set /a intentos+=1
    if !intentos! leq 10 (
        echo    → Reintento !intentos! de 10...
        choice /c SC /t 2 /d C /n >nul
        if !errorlevel! equ 1 (
            echo.
            echo [⨯] Operación cancelada por el usuario
            docker-compose down >nul 2>nul
            exit /b 1
        )
        goto :retry_connection
    ) else (
        echo [⨯] No se pudo conectar a la aplicación.
        echo    Para solucionar esto:
        echo    1. Verifica que el puerto 8080 no está bloqueado por el firewall
        echo    2. Comprueba los logs de la aplicación:
        docker-compose logs
        echo.
        echo    3. Reinicia la aplicación o ejecuta el script nuevamente
        docker-compose down >nul 2>nul
        pause
        exit /b 1
    )
)

echo.
echo [✓] ¡La aplicación está lista!
echo    → Abriendo el navegador...
start http://localhost:8080
echo.
echo    Para ver los logs: Presiona L
echo    Para salir: Presiona Q
echo.

:menu_loop
choice /c LQ /n /m "Selecciona una opción (L=Logs, Q=Salir): "
if errorlevel 2 (
    choice /c SN /n /m "¿Estás seguro que deseas salir? (S/N): "
    if errorlevel 2 goto :menu_loop
    docker-compose down >nul 2>nul
    exit
)
if errorlevel 1 (
    start cmd /k "title Logs de UML Generator && docker-compose logs -f"
    goto :menu_loop
)