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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

    public Application(UMLGeneratorProperties properties, UMLGeneratorUtil umlGeneratorUtil,
            MessageSource messageSource) {
        this.properties = properties;
        this.umlGeneratorUtil = umlGeneratorUtil;
        this.messageSource = messageSource;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "lang", required = false) String lang) {
        java.util.Locale current = LocaleContextHolder.getLocale();
        model.addAttribute("title", messageSource.getMessage("app.title", null, current));
        model.addAttribute("description", messageSource.getMessage("app.description", null, current));
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
    java.util.Locale current = LocaleContextHolder.getLocale();
        model.addAttribute("title", messageSource.getMessage("app.title", null, current));
        if (file.isEmpty()) {
            model.addAttribute("message", messageSource.getMessage("upload.selectFile", null, current));
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
            model.addAttribute("message", messageSource.getMessage("upload.ioError", null, current));
        } catch (PlantUMLExecutionException e) {
            logger.error("Error ejecutando PlantUML: {}", e.getCommand(), e);
            model.addAttribute("message", messageSource.getMessage("upload.plantumlError", null, current));
        } catch (JavaParsingException e) {
            logger.error("Error parseando archivo Java: {}", e.getFileName(), e);
            model.addAttribute("message", messageSource.getMessage("upload.javaParsingError", null, current));
        } catch (UMLGenerationException e) {
            logger.error("Error generando diagrama UML: {}", e.getMessage(), e);
            model.addAttribute("message", messageSource.getMessage("upload.umlGenError", null, current));
        } catch (Exception e) {
            logger.error("Error inesperado procesando archivo: {}", file.getOriginalFilename(), e);
            model.addAttribute("message", messageSource.getMessage("upload.unexpectedError", null, current));
        }

        return "index";
    }
}
