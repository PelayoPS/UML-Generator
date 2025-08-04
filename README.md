# UML Generator

## Descripción del Proyecto

UML-Generator es un proyecto que permite generar diagramas UML a partir del código fuente Java. Utiliza JavaParser para analizar el código y PlantUML para generar los diagramas. La aplicación está construida con Spring Boot y Thymeleaf para proporcionar una interfaz web básica.

### ✨ **Características Principales**
- **Análisis automático** de código Java con detección de clases, interfaces, métodos y atributos
- **Detección inteligente de relaciones**: herencia, implementación, asociación, composición, agregación y dependencias
- **Interfaz web moderna** con selector de temas (elegante y neobrutalista)
- **Generación de diagramas** en formato SVG de alta calidad
- **Soporte para proyectos grandes** (hasta 1GB de archivos comprimidos)
- **Procesamiento de archivos ZIP** con extracción automática

## Requisitos

- **Java 17 o superior**
- **Gradle 7.6 o superior** 
- **PlantUML** (asegúrate de que el comando `plantuml` esté disponible en tu PATH)

> **⚠️ Nota importante**: En sistemas Windows, PlantUML puede requerir configuración adicional. Los scripts de ejecución ya incluyen la compilación automática del proyecto.

### Instalación de Requisitos

1. **Java 17 o superior**:
    - Descarga e instala Java desde [aquí](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html).
    - Asegúrate de que `JAVA_HOME` esté configurado en tu PATH.
    - Verifica la instalación: `java -version`

2. **Gradle 7.6 o superior**:
    - Descarga e instala Gradle desde [aquí](https://gradle.org/install/).
    - Asegúrate de que `GRADLE_HOME` esté configurado en tu PATH.
    - Verifica la instalación: `gradle --version`
    - **Alternativa**: El proyecto incluye Gradle Wrapper (`gradlew`/`gradlew.bat`)

3. **PlantUML**:
    - Descarga e instala PlantUML desde [aquí](http://plantuml.com/download).
    - Asegúrate de que el comando `plantuml` esté disponible en tu PATH.
    - **Dependencias adicionales**: Graphviz puede ser requerido para algunos diagramas complejos

## Dependencias

Las principales dependencias del proyecto se gestionan a través de Gradle y se encuentran en el archivo `build.gradle`. Algunas de las dependencias clave incluyen:

- **Spring Boot 3.4.2**: Framework para construir aplicaciones Java con configuración automática
- **Spring Boot Web**: Para crear la API REST y servir la interfaz web
- **Thymeleaf**: Motor de plantillas para la generación de vistas HTML dinámicas
- **JavaParser 3.26.3**: Biblioteca para el análisis estático de código fuente Java
- **PlantUML**: Herramienta externa para la generación de diagramas UML a partir de texto

### 📊 **Funcionalidades del Analizador**
- Detección de **visibilidad** de métodos y atributos (`+` público, `-` privado, `#` protegido)
- Análisis de **relaciones entre clases**:
  - Herencia (`--|>`)
  - Implementación de interfaces (`..|>`)
  - Asociación (`-->`)
  - Composición (`*--`)
  - Agregación (`o--`)
  - Dependencia (`..>`)

Para ver todas las dependencias, consulta el archivo `build.gradle` en la raíz del proyecto.

## Guía de Instalación

1. Clona el repositorio:
    ```bash
    git clone https://github.com/PelayoPS/UML-Generator.git
    cd UML-Generator
    ```

2. Ejecuta el script de inicio según tu sistema operativo:
    - En Windows:
        ```bat
        run.bat
        ```
    - En Linux/Mac:
        ```bash
        ./run.sh
        ```

## Guía de Uso

### 🚀 **Inicio Rápido**

1. **Accede a la aplicación**: Abre tu navegador web y navega a `http://localhost:8080`.

2. **Prepara tu proyecto**: Comprime tu proyecto Java en un archivo `.zip` que contenga:
   - Archivos `.java` con el código fuente
   - Estructura de paquetes (opcional, pero recomendado)
   - **Límite**: Hasta 1GB de tamaño comprimido

3. **Sube y genera**: 
   - Selecciona el archivo `.zip` usando el botón "Elegir archivo"
   - Haz clic en "Generar" para procesar el archivo
   - El sistema automáticamente:
     - Descomprime el archivo
     - Analiza el código Java
     - Genera el diagrama UML
     - Muestra el resultado en formato SVG

4. **Personaliza el resultado**: 
   - Usa el **selector de temas** (esquina superior derecha) para cambiar el estilo visual
   - **Tema Elegante**: Diseño minimalista y profesional
   - **Tema Neobrutalista**: Estilo moderno con colores vibrantes

### 📁 **Formatos Soportados**
- **Entrada**: Archivos `.zip` conteniendo código fuente Java
- **Salida**: Diagramas en formato SVG (escalable y de alta calidad)
- **Análisis**: Clases, interfaces, enums, métodos, atributos y relaciones

### ⚡ **Procesamiento Automático**
- Los directorios `uploads/` y `uml_output/` se limpian automáticamente en cada ejecución
- Los archivos se procesan de forma recursiva en subdirectorios
- Se ignoran las clases nativas de Java para mantener diagramas limpios

## Estructura del Proyecto

```
UML-Generator/
├── src/main/java/com/example/demo/
│   ├── Application.java          # Controlador principal y configuración Spring Boot
│   ├── UMLGenerator.java         # Motor de análisis y generación de diagramas UML
│   ├── UMLGeneratorUtil.java     # Utilidades para manejo de archivos ZIP
│   └── WebConfig.java           # Configuración de recursos web estáticos
├── src/main/resources/
│   ├── application.properties    # Configuración de Spring (límites de archivos: 1GB)
│   ├── templates/
│   │   └── index.html           # Interfaz web con selector de temas
│   └── images/
│       └── themes.png           # Recursos gráficos
├── uploads/                     # Directorio temporal para archivos subidos
├── uml_output/                  # Directorio de salida para diagramas generados
│   ├── diagrama.puml           # Archivo PlantUML generado
│   └── diagrama.svg            # Imagen SVG del diagrama
├── build.gradle                # Configuración de dependencias y build
├── run.bat                     # Script de ejecución para Windows
└── run.sh                      # Script de ejecución para Linux/Mac
```

### 🔧 **Componentes Principales**

- **`Application.java`**: Punto de entrada, maneja las rutas web (`/` y `/upload`)
- **`UMLGenerator.java`**: Analizador de código Java que detecta clases, métodos, atributos y relaciones
- **`UMLGeneratorUtil.java`**: Maneja la descompresión de archivos ZIP y coordinación del procesamiento
- **`WebConfig.java`**: Configura el acceso a los archivos estáticos generados

## 🚨 **Solución de Problemas**

### Problemas Comunes

**❌ Error "PlantUML no encontrado"**
- Verifica que PlantUML esté instalado y en el PATH
- En Windows, puede necesitar configuración adicional de la variable `PLANTUML_JAR`

**❌ Error "Puerto 8080 ya está en uso"**
- Cierra otras aplicaciones que usen el puerto 8080
- O modifica el puerto en `application.properties`

**❌ "Archivo demasiado grande"**
- El límite actual es 1GB (configurado en `application.properties`)
- Divide proyectos muy grandes en partes más pequeñas

**❌ "No se generó el diagrama"**
- Verifica que el ZIP contenga archivos `.java` válidos
- Revisa que las clases tengan la sintaxis correcta
- Comprueba los logs en la consola para errores específicos

### Logs y Depuración
- Los logs se muestran en la consola donde se ejecuta la aplicación
- Los archivos temporales se guardan en `uploads/unzipped/`
- El archivo PlantUML generado está en `uml_output/diagrama.puml`

## 📋 **Ejemplo de Uso**

### Proyecto de Ejemplo
Si tienes un proyecto Java con la siguiente estructura:
```
MiProyecto/
├── src/
│   └── com/ejemplo/
│       ├── Usuario.java
│       ├── Producto.java
│       └── Carrito.java
└── ...
```

1. Comprime la carpeta `MiProyecto` en un archivo `MiProyecto.zip`
2. Sube el archivo a través de la interfaz web
3. El sistema generará un diagrama mostrando las relaciones entre Usuario, Producto y Carrito

### 🎯 **Qué Detecta el Analizador**
- ✅ Clases e interfaces
- ✅ Métodos públicos, privados y protegidos
- ✅ Atributos con sus tipos y visibilidad
- ✅ Herencia entre clases
- ✅ Implementación de interfaces
- ✅ Relaciones de composición y agregación
- ✅ Dependencias a través de parámetros

### ⚠️ **Limitaciones**
- Solo analiza archivos `.java` (no compila el código)
- No detecta relaciones dinámicas en tiempo de ejecución
- Las clases nativas de Java se excluyen automáticamente del diagrama
- Los comentarios y documentación no se incluyen en el diagrama

## Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un issue o un pull request para discutir cualquier cambio que te gustaría realizar.

## Licencia

Este proyecto está licenciado bajo la Licencia Apache 2.0. Consulta el archivo [`LICENSE`](./LICENSE) para más detalles.
