package com.example.demo;

import com.example.demo.config.UMLGeneratorProperties;
import com.example.demo.exception.UMLGenerationException;
import com.example.demo.exception.PlantUMLExecutionException;
import com.example.demo.exception.JavaParsingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase Application para manejar las solicitudes web.
 * 
 * @author @PelayoPS
 */
@SpringBootApplication
@Controller
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final UMLGeneratorProperties properties;
    private final UMLGeneratorUtil umlGeneratorUtil;

    public Application(UMLGeneratorProperties properties, UMLGeneratorUtil umlGeneratorUtil) {
        this.properties = properties;
        this.umlGeneratorUtil = umlGeneratorUtil;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "UML Generator");
        model.addAttribute("description", "Genera diagramas UML profesionales a partir de tu código fuente Java. Simplemente sube tu proyecto en formato .zip y obtén una visualización clara y detallada de tu arquitectura.");
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        model.addAttribute("title", "UML Generator");
        if (file.isEmpty()) {
            model.addAttribute("message", "Por favor, selecciona un archivo para subir.");
            return "index";
        }

        try {
            logger.info("Procesando archivo subido: {}", file.getOriginalFilename());

            // Usar configuración centralizada para directorios
            File uploadDir = new File(properties.getFullUploadPath());
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Guardar el archivo en uploads
            File destFile = new File(uploadDir, file.getOriginalFilename());
            file.transferTo(destFile);

            // Procesar el archivo subido usando UMLGeneratorUtil
            umlGeneratorUtil.processUploadedFile(destFile);

            // Usar configuración centralizada para URL del diagrama
            model.addAttribute("diagramUrl", properties.getDiagramUrl());
            logger.info("Archivo procesado exitosamente: {}", file.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Error de E/O al procesar archivo: {}", file.getOriginalFilename(), e);
            model.addAttribute("message", "Error al subir el archivo. Verifique permisos y espacio disponible.");
        } catch (PlantUMLExecutionException e) {
            logger.error("Error ejecutando PlantUML: {}", e.getCommand(), e);
            model.addAttribute("message", "Error generando diagrama. Verifique configuración de PlantUML.");
        } catch (JavaParsingException e) {
            logger.error("Error parseando archivo Java: {}", e.getFileName(), e);
            model.addAttribute("message", "El archivo contiene código Java inválido o corrupto.");
        } catch (UMLGenerationException e) {
            logger.error("Error generando diagrama UML: {}", e.getMessage(), e);
            model.addAttribute("message", "Error durante la generación del diagrama UML.");
        } catch (Exception e) {
            logger.error("Error inesperado procesando archivo: {}", file.getOriginalFilename(), e);
            model.addAttribute("message", "Error inesperado al procesar el archivo.");
        }

        return "index";
    }
}
