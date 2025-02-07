REM Vaciar las carpetas /uploads y /uml_output
del /Q /S uploads\*
del /Q /S uml_output\*

REM Abre el navegador en la dirección http://localhost:8080
start http://localhost:8080

REM Iniciar demo.jar
java -jar demo.jar


