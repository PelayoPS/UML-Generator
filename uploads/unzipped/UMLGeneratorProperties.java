package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para el generador UML.
 * Centraliza todas las configuraciones del proyecto para evitar hardcoding.
 * 
 * @author @PelayoPS
 */
@Component
@ConfigurationProperties(prefix = "uml.generator")
public class UMLGeneratorProperties {
    
    /**
     * Directorio donde se almacenan los archivos subidos temporalmente
     */
    private String uploadDirectory = "uploads";
    
    /**
     * Directorio de salida para los diagramas generados
     */
    private String outputDirectory = "uml_output";
    
    /**
     * Subdirectorio dentro de uploads para archivos descomprimidos
     */
    private String unzippedDirectory = "unzipped";
    
    /**
     * Ruta al archivo JAR de PlantUML (puede ser null para usar la detección automática)
     */
    private String plantUmlJarPath;
    
    /**
     * Nombre del archivo de diagrama PlantUML generado
     */
    private String diagramFileName = "diagrama";
    
    /**
     * Extensión del archivo PlantUML
     */
    private String plantUmlExtension = ".puml";
    
    /**
     * Extensión del archivo SVG generado
     */
    private String svgExtension = ".svg";
    
    // Getters y Setters
    
    public String getUploadDirectory() {
        return uploadDirectory;
    }
    
    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }
    
    public String getOutputDirectory() {
        return outputDirectory;
    }
    
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    public String getUnzippedDirectory() {
        return unzippedDirectory;
    }
    
    public void setUnzippedDirectory(String unzippedDirectory) {
        this.unzippedDirectory = unzippedDirectory;
    }
    
    public String getPlantUmlJarPath() {
        return plantUmlJarPath;
    }
    
    public void setPlantUmlJarPath(String plantUmlJarPath) {
        this.plantUmlJarPath = plantUmlJarPath;
    }
    
    public String getDiagramFileName() {
        return diagramFileName;
    }
    
    public void setDiagramFileName(String diagramFileName) {
        this.diagramFileName = diagramFileName;
    }
    
    public String getPlantUmlExtension() {
        return plantUmlExtension;
    }
    
    public void setPlantUmlExtension(String plantUmlExtension) {
        this.plantUmlExtension = plantUmlExtension;
    }
    
    public String getSvgExtension() {
        return svgExtension;
    }
    
    public void setSvgExtension(String svgExtension) {
        this.svgExtension = svgExtension;
    }
    
    /**
     * Obtiene la ruta completa del directorio de uploads
     */
    public String getFullUploadPath() {
        return System.getProperty("user.dir") + "/" + uploadDirectory;
    }
    
    /**
     * Obtiene la ruta completa del directorio de salida
     */
    public String getFullOutputPath() {
        return System.getProperty("user.dir") + "/" + outputDirectory;
    }
    
    /**
     * Obtiene la ruta completa del directorio de archivos descomprimidos
     */
    public String getFullUnzippedPath() {
        return getFullUploadPath() + "/" + unzippedDirectory;
    }
    
    /**
     * Obtiene el nombre completo del archivo PlantUML
     */
    public String getPlantUmlFileName() {
        return diagramFileName + plantUmlExtension;
    }
    
    /**
     * Obtiene el nombre completo del archivo SVG
     */
    public String getSvgFileName() {
        return diagramFileName + svgExtension;
    }
    
    /**
     * Obtiene la URL del diagrama SVG para la web
     */
    public String getDiagramUrl() {
        return "/" + outputDirectory + "/" + getSvgFileName();
    }
}
