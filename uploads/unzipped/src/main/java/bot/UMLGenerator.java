package bot;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

/**
 * Clase UMLGenerator para generar diagramas UML a partir del código fuente
 * Java.
 * 
 * @autor @PelayoPS
 */
public class UMLGenerator {
    private static final List<String> classes = new ArrayList<>();
    private static final List<String> relationships = new ArrayList<>();
    private static final Set<String> javaNativeClasses = new HashSet<>(Arrays.asList(
            "String", "Integer", "Boolean", "Long", "Double", "Float", "Character", "Byte", "Short"));

    private static String path;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java -jar UMLGenerator.jar <directorio>");
            System.exit(1);
        }
        try {
            run(args[0]);
        } catch (Exception e) {
            System.err.println("Error al generar el diagrama UML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método principal que inicia el proceso de generación del diagrama UML.
     * 
     * @param args Argumentos de la línea de comandos.
     * @throws Exception Si ocurre un error durante el procesamiento.
     */
    public static void run(String path) throws Exception {
        // Limpiar las listas antes de procesar un nuevo archivo
        classes.clear();
        relationships.clear();

        UMLGenerator.path = path;
        File srcFolder = new File(path);
        processDirectory(srcFolder);
        generatePlantUML();
        generateImageFromPlantUML();
    }

    /**
     * Procesa recursivamente un directorio y sus archivos Java.
     * 
     * @param folder El directorio a procesar.
     * @throws Exception Si ocurre un error durante el procesamiento.
     */
    private static void processDirectory(File folder) throws Exception {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                processDirectory(file);
            } else if (file.getName().endsWith(".java")) {
                processFile(file);
            }
        }
    }

    /**
     * Procesa un archivo Java para extraer información de clases, atributos y
     * métodos.
     * 
     * @param file El archivo Java a procesar.
     * @throws Exception Si ocurre un error durante el procesamiento.
     */
    private static void processFile(File file) throws Exception {
        // Parsear el archivo Java con JavaParser
        CompilationUnit cu = new JavaParser().parse(file).getResult().orElse(null);
        if (cu == null) {
            return;
        }
        // Extraer clases, atributos y métodos
        processClasses(cu);
    }

    /**
     * Procesa las clases encontradas en una unidad de compilación.
     * 
     * @param cu La unidad de compilación a procesar.
     */
    private static void processClasses(CompilationUnit cu) {
        for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = clazz.getNameAsString();
            String classType = clazz.isInterface() ? "interface" : "class";
            classes.add(classType + " " + className + " {");

            // Procesar atributos
            processAtributes(clazz.getFields());

            // Procesar métodos
            processMethods(clazz);

            // Procesar relaciones
            processRelationships(cu);

            // Fin de la clase
            classes.add("}");
        }
    }

    /**
     * Procesa los atributos de una clase.
     * Obtiene el tipo y nombre de cada atributo y los agrega a la lista de clases
     * Mostrando el tipo, nombre y visibilidad del atributo.
     * 
     * @param fields Lista de declaraciones de campos de la clase.
     */
    private static void processAtributes(List<FieldDeclaration> fields) {
        for (FieldDeclaration field : fields) {
            String type = field.getVariable(0).getTypeAsString();
            String name = field.getVariable(0).getNameAsString();
            String visibility = field.isPrivate() ? "-" : field.isProtected() ? "#" : "+";
            classes.add(visibility + " " + name + " : " + type);
        }
    }

    /**
     * Procesa los métodos de una clase.
     * Obtiene el nombre, visibilidad, tipo de retorno y parámetros de cada método
     * y los agrega a la lista de clases.
     * 
     * @param clazz La declaración de la clase o interfaz.
     */
    private static void processMethods(ClassOrInterfaceDeclaration clazz) {
        for (MethodDeclaration method : clazz.getMethods()) {
            String methodName = method.getNameAsString();
            String visibility = method.isPrivate() ? "-" : method.isProtected() ? "#" : "+";
            String returnType = method.getTypeAsString();
            StringBuilder parameters = new StringBuilder();
            for (Parameter parameter : method.getParameters()) {
                if (parameters.length() > 0) {
                    parameters.append(", ");
                }
                parameters.append(parameter.getTypeAsString()).append(" ").append(parameter.getNameAsString());
            }
            classes.add(visibility + " " + methodName + "(" + parameters.toString() + ") : " + returnType);
        }
    }

    /**
     * Procesa las relaciones entre las clases.
     * 
     * @param cu La unidad de compilación a procesar.
     */
    private static void processRelationships(CompilationUnit cu) {
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(coid -> {
            String className = coid.getNameAsString();

            if (!isClassInDirectory(className) || isJavaNativeClass(className)) {
                return;
            }

            // HERENCIA: Child --|> Parent
            coid.getExtendedTypes().forEach(extendedType -> {
                String parentClassName = extendedType.getNameAsString();
                if (isClassInDirectory(parentClassName)) {
                    relationships.add(className + " --|> " + parentClassName);
                }
            });

            // IMPLEMENTACIÓN: Class ..|> Interface
            coid.getImplementedTypes().forEach(implType -> {
                String interfaceName = implType.getNameAsString();
                if (isClassInDirectory(interfaceName)) {
                    relationships.add(className + " ..|> " + interfaceName);
                }
            });

            // ASOCIACIÓN: Class --> FieldType
            coid.getFields().forEach(field -> field.getVariables().forEach(var -> {
                String fieldType = extractGenericType(field.getElementType().toString());

                if (isClassInDirectory(fieldType)) {
                    relationships.add(className + " --> " + fieldType);
                }
            }));

            // COMPOSICIÓN vs AGREGACIÓN
            coid.getFields().forEach(field -> field.getVariables().forEach(var -> {
                String fieldType = extractGenericType(field.getElementType().toString());

                if (isClassInDirectory(fieldType)) {
                    boolean isComposition = coid.getConstructors().stream()
                            .flatMap(cons -> cons.getBody().findAll(ObjectCreationExpr.class).stream())
                            .anyMatch(expr -> expr.getType().toString().equals(fieldType));

                    if (isComposition) {
                        relationships.add(className + " *-- " + fieldType); // Composición
                    } else {
                        relationships.add(className + " o-- " + fieldType); // Agregación
                    }
                }
            }));

            // DEPENDENCIA: Class ..> Dependency (parámetros de métodos)
            coid.getMethods().forEach(method -> method.getParameters().forEach(param -> {
                String paramType = extractGenericType(param.getType().toString());

                if (isClassInDirectory(paramType)) {
                    relationships.add(className + " ..> " + paramType);
                }
            }));
        });
    }

    /**
     * Extrae el tipo genérico de una clase.
     * 
     * @param type El tipo de la clase.
     * @return El tipo genérico de la clase.
     */
    private static String extractGenericType(String type) {
        if (type.contains("<") && type.contains(">")) {
            return type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
        }
        return type;
    }

    private static boolean isJavaNativeClass(String className) {
        return javaNativeClasses.contains(className);
    }

    /**
     * Verifica si una clase está en el directorio de origen o sus subdirectorios.
     * 
     * @param className El nombre de la clase a verificar.
     * @return true si la clase está en el directorio, false en caso contrario.
     */
    private static boolean isClassInDirectory(String className) {
        File srcFolder = new File(path);
        for (File file : srcFolder.listFiles()) {
            if (file.isDirectory()) {
                if (isClassInDirectory(file, className)) {
                    return true;
                }
            } else if (file.getName().equals(className + ".java")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si una clase está en un directorio o sus subdirectorios.
     * 
     * @param File   folder: directorio a revisar
     * @param String className: nombre de la clase a buscar
     * @return true si la clase está en el directorio, false en caso contrario.
     */
    private static boolean isClassInDirectory(File folder, String className) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                if (isClassInDirectory(file, className)) {
                    return true;
                }
            } else if (file.getName().equals(className + ".java")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Genera el archivo PlantUML a partir de la información extraída.
     * 
     * @throws Exception Si ocurre un error durante la generación del archivo.
     */
    private static void generatePlantUML() throws Exception {
        File outputDir = new File("uml_output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        FileWriter writer = new FileWriter(new File(outputDir, "diagrama.puml"));
        writer.write("@startuml diagrama\n");
        createSkin(writer);
        for (String clazz : classes)
            writer.write(clazz + "\n");
        for (String rel : relationships)
            writer.write(rel + "\n");
        writer.write("@enduml\n");
        writer.close();
        System.out.println("Diagrama generado en uml_output/diagrama.puml");
    }

    /**
     * Genera una imagen SVG a partir del archivo PlantUML.
     * 
     * @throws Exception Si ocurre un error durante la generación de la imagen.
     */
    private static void generateImageFromPlantUML() throws Exception {
        try {
            // Especificar la ruta completa del ejecutable de plantuml si es necesario
            String plantUmlCommand = "plantuml";
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                plantUmlCommand = "plantuml.bat"; // Cambiar a la ruta correcta en Windows
            }

            ProcessBuilder processBuilder = new ProcessBuilder(plantUmlCommand, "-tsvg", "uml_output/diagrama.puml");
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
            System.out.println("Imagen SVG generada en uml_output/diagrama.svg");
        } catch (IOException e) {
            System.err.println("Error al ejecutar el comando plantuml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea la configuración de estilo para el diagrama PlantUML.
     * 
     * @param writer El escritor de archivos.
     * @throws IOException Si ocurre un error durante la escritura del archivo.
     */
    private static void createSkin(FileWriter writer) throws IOException {
        writer.write("!theme mono\n");
        writer.write("skinparam linetype ortho\n");
        writer.write("skinparam monochrome true\n");
    }
}