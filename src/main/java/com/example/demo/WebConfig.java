
package com.example.demo;

import com.example.demo.config.UMLGeneratorProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase WebConfig para manejar la configuración de recursos web.
 * 
 * @author @PelayoPS
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UMLGeneratorProperties properties;

    public WebConfig(UMLGeneratorProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String umlOutputPath = "file:" + properties.getFullOutputPath() + "/";
        registry.addResourceHandler("/" + properties.getOutputDirectory() + "/**")
                .addResourceLocations(umlOutputPath);
    }
}