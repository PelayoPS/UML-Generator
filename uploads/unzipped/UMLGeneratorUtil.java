package com.example.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.logging.Logger;

/**
 * Clase UMLGeneratorUtil para manejar la carga y descompresión de archivos.
 * 
 * @author @PelayoPS
 */
public class UMLGeneratorUtil {

    private static final Logger logger = Logger.getLogger(UMLGeneratorUtil.class.getName());

    /**
     * Procesa el archivo subido y genera el diagrama UML utilizando UMLGenerator.
     * 
     * @param file El archivo subido.
     * @throws Exception Si ocurre un error durante el procesamiento.
     */
    public static void processUploadedFile(File file) throws Exception {
        logger.info("Iniciando el procesamiento del archivo subido: " + file.getName());
        // Descomprimir el archivo .zip
        File destDir = new File("uploads/unzipped");
        unzip(file, destDir);

        // Obtener el directorio de destino para el diagrama UML
        File outputDir = new File("uml_output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Generar el diagrama UML
        UMLGenerator.run(destDir.getAbsolutePath());
    }

    /**
     * Descomprime un archivo .zip en un directorio de destino.
     * 
     * @param zipFile El archivo .zip a descomprimir.
     * @param destDir El directorio de destino.
     * @throws IOException Si ocurre un error durante la descompresión.
     */
    private static String unzip(File zipFile, File destDir) throws IOException {
        logger.info("Iniciando la descompresión del archivo: " + zipFile.getName());
        if (zipFile == null || destDir == null) {
            throw new IllegalArgumentException("El archivo zip o el directorio de destino no pueden ser nulos.");
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // Asegurarse de que el directorio padre exista
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
                entry = zis.getNextEntry();
            }
        }
        logger.info("Descompresión del archivo completada: " + zipFile.getName());
        //return path of new folder
        return zipFile.getAbsolutePath();
    }
}