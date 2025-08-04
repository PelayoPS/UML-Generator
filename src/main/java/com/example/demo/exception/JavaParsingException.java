package com.example.demo.exception;

/**
 * Excepción específica para archivos Java que no pueden ser parseados.
 * Proporciona información sobre el archivo problemático y la causa del fallo.
 * 
 * @author @PelayoPS
 */
public class JavaParsingException extends UMLGenerationException {
    
    private final String fileName;
    
    public JavaParsingException(String message, String fileName) {
        super(message);
        this.fileName = fileName;
    }
    
    public JavaParsingException(String message, String fileName, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    @Override
    public String toString() {
        return super.toString() + " [Archivo Java: " + fileName + "]";
    }
}
