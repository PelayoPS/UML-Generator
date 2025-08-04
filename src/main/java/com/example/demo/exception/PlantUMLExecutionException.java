package com.example.demo.exception;

/**
 * Excepción específica para errores de ejecución de PlantUML.
 * Incluye información detallada sobre el comando ejecutado y código de salida.
 * 
 * @author @PelayoPS
 */
public class PlantUMLExecutionException extends UMLGenerationException {
    
    private final String command;
    private final int exitCode;
    
    public PlantUMLExecutionException(String message, String command, int exitCode) {
        super(message);
        this.command = command;
        this.exitCode = exitCode;
    }
    
    public PlantUMLExecutionException(String message, String command, int exitCode, Throwable cause) {
        super(message, cause);
        this.command = command;
        this.exitCode = exitCode;
    }
    
    public String getCommand() {
        return command;
    }
    
    public int getExitCode() {
        return exitCode;
    }
    
    @Override
    public String toString() {
        return super.toString() + 
               " [Comando: " + command + "]" +
               " [Código salida: " + exitCode + "]";
    }
}
