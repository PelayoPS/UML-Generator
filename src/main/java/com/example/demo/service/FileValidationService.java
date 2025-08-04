package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Servicio para validación y sanitización de archivos.
 * Implementa medidas de seguridad contra path traversal y nombres maliciosos.
 * 
 * @author @PelayoPS
 */
@Service
public class FileValidationService {
    private static final Logger logger = LoggerFactory.getLogger(FileValidationService.class);
    
    // Patrón para nombres de archivo seguros (solo caracteres alfanuméricos, guiones, puntos y guiones bajos)
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    
    // Nombres de archivo peligrosos en sistemas Windows y Unix
    private static final String[] DANGEROUS_NAMES = {
        "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    };
    
    /**
     * Valida y sanitiza el nombre de un archivo.
     * 
     * @param originalFileName Nombre original del archivo
     * @return Nombre sanitizado y validado
     * @throws SecurityException Si el nombre del archivo es peligroso
     */
    public String validateAndSanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new SecurityException("Nombre de archivo vacío o nulo");
        }
        
        // Obtener solo el nombre del archivo (sin path)
        String fileName = Paths.get(originalFileName).getFileName().toString();
        
        // Verificar caracteres peligrosos
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            logger.warn("Intento de path traversal detectado: {}", originalFileName);
            throw new SecurityException("Nombre de archivo contiene caracteres de navegación de directorio");
        }
        
        // Verificar nombres reservados del sistema
        String baseName = fileName.split("\\.")[0].toUpperCase();
        for (String dangerous : DANGEROUS_NAMES) {
            if (baseName.equals(dangerous)) {
                logger.warn("Nombre de archivo reservado del sistema detectado: {}", fileName);
                throw new SecurityException("Nombre de archivo reservado del sistema: " + dangerous);
            }
        }
        
        // Verificar longitud máxima
        if (fileName.length() > 255) {
            logger.warn("Nombre de archivo demasiado largo: {} caracteres", fileName.length());
            throw new SecurityException("Nombre de archivo demasiado largo (máximo 255 caracteres)");
        }
        
        // Sanitizar caracteres especiales
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        if (!fileName.equals(sanitized)) {
            logger.info("Nombre de archivo sanitizado: '{}' -> '{}'", fileName, sanitized);
        }
        
        return sanitized;
    }
    
    /**
     * Valida el tamaño de un archivo.
     * 
     * @param fileSize Tamaño del archivo en bytes
     * @param maxSize Tamaño máximo permitido en bytes
     * @throws SecurityException Si el archivo excede el tamaño máximo
     */
    public void validateFileSize(long fileSize, long maxSize) {
        if (fileSize > maxSize) {
            logger.warn("Archivo demasiado grande: {} bytes (máximo: {} bytes)", fileSize, maxSize);
            throw new SecurityException(String.format("Archivo demasiado grande: %d MB (máximo: %d MB)", 
                fileSize / (1024 * 1024), maxSize / (1024 * 1024)));
        }
        
        if (fileSize <= 0) {
            throw new SecurityException("Archivo vacío o inválido");
        }
    }
    
    /**
     * Valida el tipo MIME de un archivo.
     * 
     * @param contentType Tipo MIME del archivo
     * @param allowedTypes Tipos MIME permitidos
     * @throws SecurityException Si el tipo MIME no está permitido
     */
    public void validateContentType(String contentType, String... allowedTypes) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new SecurityException("Tipo de contenido no especificado");
        }
        
        for (String allowedType : allowedTypes) {
            if (contentType.toLowerCase().contains(allowedType.toLowerCase())) {
                return;
            }
        }
        
        logger.warn("Tipo de contenido no permitido: {}", contentType);
        throw new SecurityException("Tipo de archivo no permitido: " + contentType);
    }
    
    /**
     * Valida que una entrada ZIP no sea una "zip bomb".
     * 
     * @param compressedSize Tamaño comprimido
     * @param uncompressedSize Tamaño descomprimido estimado
     * @throws SecurityException Si la ratio de compresión es sospechosa
     */
    public void validateCompressionRatio(long compressedSize, long uncompressedSize) {
        if (compressedSize <= 0 || uncompressedSize <= 0) {
            return; // No validar si no tenemos datos válidos
        }
        
        double ratio = (double) uncompressedSize / compressedSize;
        
        // Ratio sospechosa (mayor a 100:1)
        if (ratio > 100) {
            logger.warn("Ratio de compresión sospechosa detectada: {}:1", Math.round(ratio));
            throw new SecurityException("Archivo ZIP con ratio de compresión sospechosa (posible zip bomb)");
        }
    }
}
