package com.example.demo;

import com.example.demo.config.UMLGeneratorProperties;
import com.example.demo.exception.UMLGenerationException;
import com.example.demo.service.FileValidationService;
import com.example.demo.service.AnnotationAnalysisService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase UMLGeneratorUtil para manejar la carga y descompresión de archivos.
 * 
 * @author @PelayoPS
 */
@Component
public class UMLGeneratorUtil {

    private static final Logger logger = LoggerFactory.getLogger(UMLGeneratorUtil.class);
    private static final long MAX_EXTRACTED_SIZE = 1024 * 1024 * 1024; // 1GB límite de extracción

    private final UMLGeneratorProperties properties;
    private final UMLGenerator umlGenerator;
    private final FileValidationService fileValidationService;
    private final AnnotationAnalysisService annotationAnalysisService;

    public UMLGeneratorUtil(UMLGeneratorProperties properties, UMLGenerator umlGenerator,
            FileValidationService fileValidationService,
            AnnotationAnalysisService annotationAnalysisService) {
        this.properties = properties;
        this.umlGenerator = umlGenerator;
        this.fileValidationService = fileValidationService;
        this.annotationAnalysisService = annotationAnalysisService;
    }

    /**
     * Procesa el archivo subido y genera el diagrama UML utilizando UMLGenerator.
     * 
     * @param file El archivo subido.
     * @throws UMLGenerationException Si ocurre un error durante el procesamiento.
     */
    public void processUploadedFile(File file) throws UMLGenerationException {
        logger.info("Iniciando procesamiento del archivo subido: {}", file.getName());
        try {
            // Descomprimir el archivo .zip usando configuración centralizada
            File destDir = new File(properties.getFullUnzippedPath());
            unzip(file, destDir);

            // Obtener el directorio de destino para el diagrama UML usando configuración
            // centralizada
            File outputDir = new File(properties.getFullOutputPath());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Usar el servicio UMLGenerator inyectado
            umlGenerator.generateDiagram(destDir.getAbsolutePath());

            logger.info("Procesamiento completado exitosamente para archivo: {}", file.getName());
        } catch (IOException e) {
            throw new UMLGenerationException("Error al descomprimir archivo ZIP",
                    "UMLGeneratorUtil", file.getName(), e);
        }
    }

    /**
     * Descomprime un archivo .zip en un directorio de destino.
     * 
     * @param zipFile El archivo .zip a descomprimir.
     * @param destDir El directorio de destino.
     * @throws IOException Si ocurre un error durante la descompresión.
     */
    /**
     * Descomprime un archivo .zip en un directorio de destino con validaciones de
     * seguridad.
     * 
     * @param zipFile El archivo .zip a descomprimir
     * @param destDir El directorio de destino
     * @return Ruta del directorio raíz extraído
     * @throws IOException Si ocurre un error durante la descompresión
     */
    private String unzip(File zipFile, File destDir) throws IOException {
        logger.info("Iniciando la descompresión del archivo: " + zipFile.getName());

        if (zipFile == null || destDir == null) {
            throw new IllegalArgumentException("El archivo zip o el directorio de destino no pueden ser nulos.");
        }

        // Validaciones de seguridad
        fileValidationService.validateFileSize(zipFile.length(), MAX_EXTRACTED_SIZE);

        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        String rootDir = null;
        long totalExtractedSize = 0;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();

            while (entry != null) {
                // Validar nombre del archivo
                String sanitizedName = fileValidationService.validateAndSanitizeFileName(entry.getName());
                File newFile = new File(destDir, sanitizedName);

                // Verificar que el archivo esté dentro del directorio de destino (anti-path
                // traversal)
                String canonicalDestPath = destDir.getCanonicalPath();
                String canonicalNewFilePath = newFile.getCanonicalPath();

                if (!canonicalNewFilePath.startsWith(canonicalDestPath + File.separator)) {
                    logger.warn("Intento de path traversal detectado: {}", entry.getName());
                    zis.closeEntry();
                    entry = zis.getNextEntry();
                    continue;
                }

                // Control de tamaño total extraído
                totalExtractedSize += entry.getSize();
                if (totalExtractedSize > MAX_EXTRACTED_SIZE) {
                    throw new IOException("El archivo ZIP excede el límite de extracción (" +
                            (MAX_EXTRACTED_SIZE / (1024 * 1024)) + " MB)");
                }

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    if (rootDir == null) {
                        rootDir = newFile.getAbsolutePath();
                    }
                } else {
                    // Asegurar que el directorio padre exista
                    new File(newFile.getParent()).mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        long fileExtractedSize = 0;

                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                            fileExtractedSize += len;

                            // Control de tamaño por archivo individual
                            if (fileExtractedSize > 100 * 1024 * 1024) { // 100MB por archivo
                                throw new IOException("Archivo individual demasiado grande: " + sanitizedName);
                            }
                        }
                    }

                    if (rootDir == null) {
                        rootDir = newFile.getParent();
                    }
                }

                zis.closeEntry();
                entry = zis.getNextEntry();
            }
        }

        logger.info("Descompresión del archivo completada: {} (Total extraído: {} MB)",
                zipFile.getName(), totalExtractedSize / (1024 * 1024));

        return rootDir != null ? rootDir : destDir.getAbsolutePath();
    }
}