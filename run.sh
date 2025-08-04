# Vaciar las carpetas /uploads y /uml_output, incluyendo subdirectorios
rm -rf uploads/*
rm -rf uml_output/*

# Compilar el proyecto
./gradlew clean build

# Abre el navegador en la dirección http://localhost:8080
xdg-open http://localhost:8080

# Iniciar demo.jar
java -jar build/libs/app.jar

