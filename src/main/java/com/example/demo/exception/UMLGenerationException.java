package com.example.demo.exception;

/**
 * Excepción específica para errores durante la generación de diagramas UML.
 * Proporciona contexto específico sobre fallos en el proceso de análisis o generación.
 * 
 * @author @PelayoPS
 */
public class UMLGenerationException extends Exception {
    
    private final String component;
    private final String filePath;
    
    public UMLGenerationException(String message) {
        super(message);
        this.component = null;
        this.filePath = null;
    }
    
    public UMLGenerationException(String message, Throwable cause) {
        super(message, cause);
        this.component = null;
        this.filePath = null;
    }
    
    public UMLGenerationException(String message, String component, String filePath) {
        super(message);
        this.component = component;
        this.filePath = filePath;
    }
    
    public UMLGenerationException(String message, String component, String filePath, Throwable cause) {
        super(message, cause);
        this.component = component;
        this.filePath = filePath;
    }
    
    public String getComponent() {
        return component;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (component != null) {
            sb.append(" [Componente: ").append(component).append("]");
        }
        if (filePath != null) {
            sb.append(" [Archivo: ").append(filePath).append("]");
        }
        return sb.toString();
    }
}
