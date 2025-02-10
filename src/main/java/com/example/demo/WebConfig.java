
package com.example.demo;

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
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String umlOutputPath = "file:" + System.getProperty("user.dir") + "/uml_output/";
        registry.addResourceHandler("/uml_output/**").addResourceLocations(umlOutputPath);
    }
}