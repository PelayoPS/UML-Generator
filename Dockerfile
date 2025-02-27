FROM eclipse-temurin:17-jdk-alpine

# Instalar dependencias necesarias
RUN apk add --no-cache graphviz ttf-dejavu fontconfig gradle curl

# Crear directorio para PlantUML y scripts
RUN mkdir -p /opt/plantuml

# Descargar e instalar PlantUML
RUN curl -L https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml-1.2024.0.jar \
    -o /opt/plantuml/plantuml.jar

# Crear script wrapper para PlantUML
RUN printf '#!/bin/sh\njava -jar /opt/plantuml/plantuml.jar "$@"' > /usr/local/bin/plantuml && \
    chmod +x /usr/local/bin/plantuml

# Configurar variables de entorno para PlantUML
ENV PLANTUML_JAR=/opt/plantuml/plantuml.jar
ENV GRAPHVIZ_DOT=/usr/bin/dot
ENV PATH="/usr/local/bin:${PATH}"

# Verificar la instalación usando la ruta completa
RUN java -jar /opt/plantuml/plantuml.jar -version

WORKDIR /app

# Copiar los archivos del proyecto
COPY . .

# Compilar el proyecto
RUN gradle clean build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/app.jar"]