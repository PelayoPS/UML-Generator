# UML Generator

## Descripción del Proyecto

UML-Generator es un proyecto que permite generar diagramas UML a partir del código fuente Java. Utiliza JavaParser para analizar el código y PlantUML para generar los diagramas. La aplicación está construida con Spring Boot y Thymeleaf para proporcionar una interfaz web básica.

## Requisitos

- Java 17 o superior
- Gradle 7.6 o superior
- PlantUML (asegúrate de que el comando `plantuml` esté disponible en tu PATH)

### Instalación de Requisitos

1. **Java 17 o superior**:
    - Descarga e instala Java desde [aquí](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html).
    - Asegúrate de que `JAVA_HOME` esté configurado en tu PATH.

2. **Gradle 7.6 o superior**:
    - Descarga e instala Gradle desde [aquí](https://gradle.org/install/).
    - Asegúrate de que `GRADLE_HOME` esté configurado en tu PATH.

3. **PlantUML**:
    - Descarga e instala PlantUML desde [aquí](http://plantuml.com/download).
    - Asegúrate de que el comando `plantuml` esté disponible en tu PATH.

## Dependencias

Las principales dependencias del proyecto se gestionan a través de Gradle y se encuentran en el archivo `build.gradle`. Algunas de las dependencias clave incluyen:

- **Spring Boot**: Framework para construir aplicaciones Java.
- **Thymeleaf**: Motor de plantillas para la generación de vistas HTML.
- **JavaParser**: Biblioteca para el análisis de código fuente Java.
- **PlantUML**: Herramienta para la generación de diagramas UML a partir de texto.

Para ver todas las dependencias, consulta el archivo `build.gradle` en la raíz del proyecto.

## Guía de Instalación

1. Clona el repositorio:
    ```bash
    git clone https://github.com/PelayoPS/UML-Generator.git
    cd UML-Generator
    ```

2. Construye el proyecto con Gradle:
    ```bash
    gradle build
    ```

3. Ejecuta el script de inicio:
    - En Windows:
        ```bat
        run.bat
        ```
    - En Linux/Mac:
        ```bash
        ./run.sh
        ```

## Guía de Uso

1. Abre tu navegador web y navega a `http://localhost:8080`.

2. Sube un archivo `.zip` que contenga el código fuente Java del cual deseas generar el diagrama UML.

3. Haz clic en el botón "Generar" para procesar el archivo y generar el diagrama UML.

4. Una vez generado, el diagrama UML se mostrará en la página web.

## Estructura del Proyecto

- `/src/main/java/com/example/demo`: Contiene el código fuente principal de la aplicación.
- `/src/main/resources/templates`: Contiene las plantillas Thymeleaf para la interfaz web.
- `/uploads`: Directorio donde se almacenan los archivos subidos.
- `/uml_output`: Directorio donde se almacenan los diagramas UML generados.

## Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un issue o un pull request para discutir cualquier cambio que te gustaría realizar.

## Licencia

Este proyecto está licenciado bajo la Licencia Apache 2.0. Consulta el archivo [`LICENSE`](./LICENSE) para más detalles.
