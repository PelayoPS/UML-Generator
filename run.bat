@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM -------------------------------------------------------------
REM Resolver JAVA_HOME si no está definido (detección en rutas comunes)
REM -------------------------------------------------------------
if "%JAVA_HOME%"=="" call :resolve_java
if "%JAVA_HOME%"=="" goto :no_java

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Usando JAVA_HOME: %JAVA_HOME%
java -version

REM -------------------------------------------------------------
REM Vaciar las carpetas /uploads y /uml_output
REM -------------------------------------------------------------
if exist uploads del /Q /S "uploads\*" 2>nul
if exist uml_output del /Q /S "uml_output\*" 2>nul

REM -------------------------------------------------------------
REM Compilar el proyecto
REM -------------------------------------------------------------
call gradlew clean build || goto :build_fail

REM -------------------------------------------------------------
REM Abre el navegador en la dirección http://localhost:8080
REM -------------------------------------------------------------
start "" http://localhost:8080

REM -------------------------------------------------------------
REM Iniciar la aplicación
REM -------------------------------------------------------------
if not exist "build\libs\app.jar" (
	echo [ERROR] No se encontró build\libs\app.jar. Verifica que el build haya generado el artefacto esperado.
	exit /b 2
)
call java -jar "build\libs\app.jar"

goto :eof

:build_fail
echo [ERROR] Fallo en la compilacion con Gradle.
exit /b 3

:no_java
echo [ERROR] No se ha encontrado un JDK. Instala JDK 21+ (Adoptium/Microsoft/Oracle/Corretto) o define JAVA_HOME antes de ejecutar este script.
exit /b 1

:resolve_java
REM Buscar JDK en rutas comunes
for %%D in (
	"C:\\Program Files\\Eclipse Adoptium\\jdk-2*"
	"C:\\Program Files\\Microsoft\\jdk-2*"
	"C:\\Program Files\\Java\\jdk-2*"
	"C:\\Program Files\\Zulu\\zulu-2*"
	"C:\\Program Files\\Amazon Corretto\\jdk*"
	"C:\\Program Files\\AdoptOpenJDK\\jdk*"
) do (
	for /d %%P in (%%D) do (
		if not defined JAVA_HOME if exist "%%P\bin\java.exe" set "JAVA_HOME=%%P"
	)
)
exit /b 0


