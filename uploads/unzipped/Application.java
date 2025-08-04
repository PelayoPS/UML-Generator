package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import java.io.IOException;

/**
 * Clase Application para manejar las solicitudes web.
 * 
 * @author @PelayoPS
 */
@SpringBootApplication
@Controller
public class Application implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "UML Generator");
        model.addAttribute("description", "Genera diagramas UML profesionales a partir de tu código fuente Java. Simplemente sube tu proyecto en formato .zip y obtén una visualización clara y detallada de tu arquitectura.");
        return "index";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uml_output/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uml_output/");
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        model.addAttribute("title", "UML Generator");
        if (file.isEmpty()) {
            model.addAttribute("message", "Por favor, selecciona un archivo para subir.");
            return "index";
        }

        try {
            // Directorio uploads en el proyecto
            File uploadDir = new File(System.getProperty("user.dir"), "uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Guardar el archivo en uploads
            File destFile = new File(uploadDir, file.getOriginalFilename());
            file.transferTo(destFile);

            // Procesar el archivo subido usando UMLGeneratorUtil
            UMLGeneratorUtil.processUploadedFile(destFile);

            // Agregar la url de la imagen generada al modelo
            model.addAttribute("diagramUrl", "/uml_output/diagrama.svg");
        } catch (IOException e) {
            model.addAttribute("message", "Error al subir el archivo: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("message", "Error al procesar el archivo: " + e.getMessage());
        }

        return "index";
    }
}
