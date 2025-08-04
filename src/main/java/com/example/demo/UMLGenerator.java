package com.example.demo;

import com.example.demo.config.UMLGeneratorProperties;
import com.example.demo.exception.UMLGenerationException;
import com.example.demo.exception.PlantUMLExecutionException;
import com.example.demo.exception.JavaParsingException;
import com.example.demo.service.AnnotationAnalysisService;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.type.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio UMLGenerator para generar diagramas UML a partir del código fuente
 * Java.
 * Convertido a servicio Spring thread-safe para soportar procesamiento
 * concurrente.
 * 
 * @author @PelayoPS
 */
@Service
public class UMLGenerator {
    private static final Logger logger = LoggerFactory.getLogger(UMLGenerator.class);

    private static final Set<String> JAVA_NATIVE_CLASSES = new HashSet<>(Arrays.asList(
            // Tipos primitivos y wrappers
            "String", "Integer", "Boolean", "Long", "Double", "Float", "Character", "Byte", "Short",
            "int", "boolean", "long", "double", "float", "char", "byte", "short", "void",
            // Clases fundamentales de Java
            "Object", "Class", "Exception", "RuntimeException", "Throwable", "System", "Thread",
            // Colecciones más comunes
            "List", "ArrayList", "LinkedList", "Set", "HashSet", "LinkedHashSet", "TreeSet",
            "Map", "HashMap", "LinkedHashMap", "TreeMap", "Collection", "Queue", "Deque",
            // Utilerías comunes
            "Date", "Calendar", "LocalDate", "LocalDateTime", "Optional", "Stream",
            "BigDecimal", "BigInteger", "UUID", "Pattern", "Matcher",
            // Spring Framework (comunes)
            "Model", "RedirectAttributes", "HttpServletRequest", "HttpServletResponse",
            "MultipartFile", "ResponseEntity", "RequestMapping", "GetMapping", "PostMapping",
            // Tipos de retorno comunes
            "ResponseEntity", "Void"));

    private final UMLGeneratorProperties properties;
    private final AnnotationAnalysisService annotationAnalysisService;

    public UMLGenerator(UMLGeneratorProperties properties, AnnotationAnalysisService annotationAnalysisService) {
        this.properties = properties;
        this.annotationAnalysisService = annotationAnalysisService;
    }

    /**
     * Contexto de análisis thread-safe que contiene el estado para una sesión de
     * análisis
     */
    public static class AnalysisContext {
        private final List<String> classes = new CopyOnWriteArrayList<>();
        private final List<String> relationships = new CopyOnWriteArrayList<>();
        private final Set<String> processedFiles = ConcurrentHashMap.newKeySet();
        private final String basePath;

        public AnalysisContext(String basePath) {
            this.basePath = basePath;
        }

        public List<String> getClasses() {
            return classes;
        }

        public List<String> getRelationships() {
            return relationships;
        }

        public Set<String> getProcessedFiles() {
            return processedFiles;
        }

        public String getBasePath() {
            return basePath;
        }
    }

    /**
     * Método principal que inicia el proceso de generación del diagrama UML.
     * Ahora thread-safe y con soporte para procesamiento paralelo.
     * 
     * @param path Ruta del directorio a procesar
     * @return Contexto de análisis con los resultados
     * @throws UMLGenerationException Si ocurre un error durante el procesamiento.
     */
    public AnalysisContext generateDiagram(String path) throws UMLGenerationException {
        logger.info("Iniciando generación de diagrama UML para directorio: {}", path);
        logger.debug("Configuración utilizada: outputDir={}, plantUmlJar={}",
                properties.getOutputDirectory(), properties.getPlantUmlJarPath());

        try {
            File srcFolder = new File(path);

            if (!srcFolder.exists() || !srcFolder.isDirectory()) {
                throw new UMLGenerationException("El directorio especificado no existe o no es válido: " + path);
            }

            AnalysisContext context = new AnalysisContext(path);

            logger.debug("Procesando directorio fuente: {}", srcFolder.getAbsolutePath());
            processDirectory(srcFolder, context);

            logger.info("Procesamiento completado. Clases encontradas: {}, Relaciones: {}",
                    context.getClasses().size(), context.getRelationships().size());

            generatePlantUML(context);
            generateImageFromPlantUML();

            logger.info("Generación de diagrama UML completada exitosamente");
            return context;
        } catch (Exception e) {
            if (e instanceof UMLGenerationException) {
                throw (UMLGenerationException) e;
            }
            throw new UMLGenerationException("Error inesperado durante la generación del diagrama UML", e);
        }
    }

    /**
     * Procesa recursivamente un directorio y sus archivos Java.
     * 
     * @param folder  El directorio a procesar
     * @param context Contexto de análisis thread-safe
     * @throws JavaParsingException Si ocurre un error al parsear un archivo Java
     */
    private void processDirectory(File folder, AnalysisContext context) throws JavaParsingException {
        File[] files = folder.listFiles();
        if (files == null) {
            logger.warn("No se pueden listar archivos en directorio: {}", folder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, context);
            } else if (file.getName().endsWith(".java")) {
                logger.debug("Procesando archivo Java: {}", file.getName());
                processFile(file, context);
            }
        }
    }

    /**
     * Procesa un archivo Java para extraer información de clases, atributos y
     * métodos.
     * 
     * @param file    El archivo Java a procesar
     * @param context Contexto de análisis thread-safe
     * @throws JavaParsingException Si ocurre un error al parsear el archivo
     */
    private void processFile(File file, AnalysisContext context) throws JavaParsingException {
        try {
            // Evitar procesamiento duplicado
            if (!context.getProcessedFiles().add(file.getAbsolutePath())) {
                return;
            }

            // Parsear el archivo Java con JavaParser
            CompilationUnit cu = new JavaParser().parse(file).getResult().orElse(null);
            if (cu == null) {
                logger.warn("No se pudo parsear el archivo Java: {}", file.getName());
                return;
            }
            // Extraer clases, atributos y métodos
            processClasses(cu, context);
        } catch (Exception e) {
            throw new JavaParsingException("Error al parsear archivo Java: " + e.getMessage(),
                    file.getName(), e);
        }
    }

    /**
     * Procesa las clases encontradas en una unidad de compilación.
     * Incluye análisis de anotaciones para detectar estereotipos.
     * 
     * @param cu      La unidad de compilación a procesar
     * @param context Contexto de análisis thread-safe
     */
    private void processClasses(CompilationUnit cu, AnalysisContext context) {
        List<String> classDefinitions = new ArrayList<>();

        for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            StringBuilder classBuilder = new StringBuilder();

            // Usar el servicio de análisis de anotaciones para generar la línea de clase
            String classLineWithAnnotations = annotationAnalysisService.generateClassLineWithAnnotations(clazz);
            classBuilder.append(classLineWithAnnotations).append("\n");

            // Procesar atributos
            for (FieldDeclaration field : clazz.getFields()) {
                String type = field.getVariable(0).getTypeAsString();
                String name = field.getVariable(0).getNameAsString();
                String visibility = field.isPrivate() ? "-" : field.isProtected() ? "#" : "+";
                classBuilder.append(visibility).append(" ").append(name).append(" : ").append(type).append("\n");
            }

            // Procesar métodos
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
                classBuilder.append(visibility).append(" ").append(methodName).append("(")
                        .append(parameters.toString()).append(") : ").append(returnType).append("\n");
            }

            // Fin de la clase
            classBuilder.append("}");

            classDefinitions.add(classBuilder.toString());
        }

        // Agregar todas las definiciones de clase de este archivo al contexto
        for (String classDef : classDefinitions) {
            context.getClasses().add(classDef);
        }

        // Procesar relaciones una sola vez por archivo, no por cada clase
        processRelationships(cu, context);

        // Generar estadísticas de anotaciones para logging
        annotationAnalysisService.generateAnnotationStatistics(cu);

        // Detectar patrones arquitectónicos
        List<String> patterns = annotationAnalysisService.detectArchitecturalPatterns(cu);
        if (!patterns.isEmpty()) {
            logger.info("Patrones arquitectónicos detectados en {}: {}",
                    cu.getPrimaryTypeName().orElse("archivo"), patterns);
        }
    }

    /**
     * Procesa las relaciones entre las clases de forma exhaustiva y completa.
     * Analiza TODOS los tipos de relaciones posibles en el código Java.
     * 
     * @param cu      La unidad de compilación a procesar
     * @param context Contexto de análisis thread-safe
     */
    private void processRelationships(CompilationUnit cu, AnalysisContext context) {
        // Set para evitar relaciones duplicadas
        Set<String> addedRelationships = new HashSet<>();

        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = classDecl.getNameAsString();

            if (isJavaNativeClass(className)) {
                continue;
            }

            // 1. HERENCIA: Child --|> Parent
            classDecl.getExtendedTypes().forEach(extendedType -> {
                String parentClassName = extractSimpleClassName(extendedType.asString());
                if (isValidClassForRelation(parentClassName, context)) {
                    addRelationship(addedRelationships, context, className + " --|> " + parentClassName, "Herencia");
                }
            });

            // 2. IMPLEMENTACIÓN: Class ..|> Interface
            classDecl.getImplementedTypes().forEach(implType -> {
                String interfaceName = extractSimpleClassName(implType.asString());
                if (isValidClassForRelation(interfaceName, context)) {
                    addRelationship(addedRelationships, context, className + " ..|> " + interfaceName,
                            "Implementación");
                }
            });

            // 3. ANÁLISIS COMPLETO DE CONSTRUCTORES
            classDecl.getConstructors().forEach(constructor -> {
                // Parámetros del constructor (inyección de dependencias)
                constructor.getParameters().forEach(param -> {
                    String paramType = extractSimpleClassName(param.getType().asString());
                    if (isValidClassForRelation(paramType, context)) {
                        addRelationship(addedRelationships, context, className + " ..> " + paramType + " : <<inject>>",
                                "Inyección constructor");
                    }
                });

                // Instanciaciones dentro del constructor
                constructor.getBody().findAll(ObjectCreationExpr.class).forEach(creation -> {
                    String createdType = extractSimpleClassName(creation.getType().asString());
                    if (isValidClassForRelation(createdType, context)) {
                        addRelationship(addedRelationships, context,
                                className + " ..> " + createdType + " : <<creates>>", "Creación en constructor");
                    }
                });

                // Llamadas a métodos en el constructor
                constructor.getBody().findAll(MethodCallExpr.class).forEach(methodCall -> {
                    if (methodCall.getScope().isPresent()) {
                        String scopeType = extractTypeFromExpression(methodCall.getScope().get().toString(), cu);
                        if (isValidClassForRelation(scopeType, context)) {
                            addRelationship(addedRelationships, context,
                                    className + " ..> " + scopeType + " : <<uses>>", "Uso en constructor");
                        }
                    }
                });
            });

            // 4. ANÁLISIS EXHAUSTIVO DE CAMPOS/ATRIBUTOS
            classDecl.getFields().forEach(field -> {
                field.getVariables().forEach(variable -> {
                    String fieldType = extractSimpleClassName(field.getElementType().asString());

                    if (isValidClassForRelation(fieldType, context)) {
                        String relationshipType = determineFieldRelationshipType(field, classDecl, fieldType);
                        addRelationship(addedRelationships, context,
                                className + " " + relationshipType + " " + fieldType, "Campo/Atributo");
                    }
                });
            });

            // 5. ANÁLISIS COMPLETO DE MÉTODOS
            classDecl.getMethods().forEach(method -> {
                // Parámetros de métodos
                method.getParameters().forEach(param -> {
                    String paramType = extractSimpleClassName(param.getType().asString());
                    if (isValidClassForRelation(paramType, context)) {
                        addRelationship(addedRelationships, context, className + " ..> " + paramType + " : <<uses>>",
                                "Parámetro método");
                    }
                });

                // Tipos de retorno
                String returnType = extractSimpleClassName(method.getType().asString());
                if (isValidClassForRelation(returnType, context) && !returnType.equals("void")) {
                    addRelationship(addedRelationships, context, className + " ..> " + returnType + " : <<returns>>",
                            "Tipo retorno");
                }

                // Instanciaciones dentro del método (new Class())
                method.getBody().ifPresent(body -> {
                    body.findAll(ObjectCreationExpr.class).forEach(creation -> {
                        String createdType = extractSimpleClassName(creation.getType().asString());
                        if (isValidClassForRelation(createdType, context)) {
                            addRelationship(addedRelationships, context,
                                    className + " ..> " + createdType + " : <<creates>>", "Creación en método");
                        }
                    });

                    // Variables locales tipadas
                    body.findAll(VariableDeclarationExpr.class).forEach(varDecl -> {
                        String varType = extractSimpleClassName(varDecl.getElementType().asString());
                        if (isValidClassForRelation(varType, context)) {
                            addRelationship(addedRelationships, context, className + " ..> " + varType + " : <<uses>>",
                                    "Variable local");
                        }
                    });

                    // Llamadas a métodos (Class.method() o object.method())
                    body.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        if (methodCall.getScope().isPresent()) {
                            String scopeType = extractTypeFromExpression(methodCall.getScope().get().toString(), cu);
                            if (isValidClassForRelation(scopeType, context)) {
                                addRelationship(addedRelationships, context,
                                        className + " ..> " + scopeType + " : <<calls>>", "Llamada método");
                            }
                        }
                    });
                });
            });

            // 6. ANÁLISIS DE IMPORTS - Solo del proyecto actual
            analyzeImportsForRelationships(cu, className, context, addedRelationships);

            // 7. RELACIONES POR ANOTACIONES SPRING
            analyzeSpringAnnotationRelationships(classDecl, className, context, addedRelationships);

            // 8. RELACIONES CON CLASES ANIDADAS (Inner/Nested Classes)
            analyzeNestedClassRelationships(classDecl, className, context, addedRelationships);
        }

        // 9. RELACIONES ESTÁTICAS (llamadas a métodos estáticos de otras clases)
        analyzeStaticMethodRelationships(cu, context, addedRelationships);

        // 10. RELACIONES POR MANEJO DE EXCEPCIONES (throws, catch)
        analyzeExceptionRelationships(cu, context, addedRelationships);

        // 11. RELACIONES POR TIPOS GENÉRICOS (generics)
        analyzeGenericTypeRelationships(cu, context, addedRelationships);

        // 12. RELACIONES POR SOBRESCRITURA DE MÉTODOS (@Override)
        analyzeMethodOverrideRelationships(cu, context, addedRelationships);

        // 13. RELACIONES POR PROGRAMACIÓN FUNCIONAL (Lambda, Method References, Stream
        // API)
        analyzeFunctionalProgrammingRelationships(cu, context, addedRelationships);

        // 14. RELACIONES POR TYPE INFERENCE (var keyword)
        analyzeTypeInferenceRelationships(cu, context, addedRelationships);

        // 15. RELACIONES POR INTERFACES FUNCIONALES (Supplier, Consumer, Function,
        // Predicate)
        analyzeFunctionalInterfaceRelationships(cu, context, addedRelationships);

        logger.info("Total de relaciones procesadas: {}", addedRelationships.size());
    }

    /**
     * Añade una relación evitando duplicados y loggeando la acción.
     */
    private void addRelationship(Set<String> addedRelationships, AnalysisContext context,
            String relationship, String type) {
        if (addedRelationships.add(relationship)) {
            context.getRelationships().add(relationship);
            logger.debug("{} detectada: {}", type, relationship);
        }
    }

    /**
     * Extrae el tipo de una expresión analizando el contexto.
     */
    private String extractTypeFromExpression(String expression, CompilationUnit cu) {
        // Si es una llamada a método estática (ClassName.method)
        if (expression.contains(".") && Character.isUpperCase(expression.charAt(0))) {
            String className = expression.substring(0, expression.indexOf("."));
            return extractSimpleClassName(className);
        }

        // Si es una variable, buscar su tipo en el contexto
        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            // Buscar en campos
            for (FieldDeclaration field : classDecl.getFields()) {
                if (field.getVariables().stream().anyMatch(var -> var.getNameAsString().equals(expression))) {
                    return extractSimpleClassName(field.getElementType().asString());
                }
            }
        }

        return extractSimpleClassName(expression);
    }

    /**
     * Analiza relaciones implícitas por anotaciones de Spring.
     */
    private void analyzeSpringAnnotationRelationships(ClassOrInterfaceDeclaration classDecl, String className,
            AnalysisContext context, Set<String> addedRelationships) {

        // Si es un @Controller, puede usar @Service
        boolean isController = classDecl.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals("Controller")
                        || ann.getNameAsString().equals("RestController"));

        // Si es un @Service, puede usar @Repository o otros @Service
        boolean isService = classDecl.getAnnotations().stream()
                .anyMatch(ann -> ann.getNameAsString().equals("Service"));

        if (isController || isService) {
            // Buscar en otros archivos del proyecto clases que podrían ser inyectadas
            // (Esto se puede expandir para ser más específico)
            for (String otherClass : extractAllClassNamesFromContext(context)) {
                if (!otherClass.equals(className) && isValidClassForRelation(otherClass, context)) {
                    // Relación probable de inyección por arquitectura Spring
                    if (isController && otherClass.contains("Service")) {
                        addRelationship(addedRelationships, context,
                                className + " ..> " + otherClass + " : <<likely-inject>>", "Probable inyección Spring");
                    } else if (isService && (otherClass.contains("Repository") || otherClass.contains("Service"))) {
                        addRelationship(addedRelationships, context,
                                className + " ..> " + otherClass + " : <<likely-inject>>", "Probable inyección Spring");
                    }
                }
            }
        }
    }

    /**
     * Analiza relaciones con clases anidadas (inner/nested classes).
     */
    private void analyzeNestedClassRelationships(ClassOrInterfaceDeclaration classDecl, String className,
            AnalysisContext context, Set<String> addedRelationships) {

        // Buscar clases anidadas dentro de esta clase
        classDecl.findAll(ClassOrInterfaceDeclaration.class).forEach(nestedClass -> {
            if (!nestedClass.equals(classDecl)) { // Evitar la clase padre
                String nestedClassName = nestedClass.getNameAsString();

                // Relación de composición entre clase padre e hija
                addRelationship(addedRelationships, context,
                        className + " *-- " + nestedClassName + " : <<nested>>", "Clase anidada");

                logger.debug("Clase anidada detectada: {} dentro de {}", nestedClassName, className);
            }
        });
    }

    /**
     * Analiza relaciones con llamadas a métodos estáticos de otras clases.
     */
    private void analyzeStaticMethodRelationships(CompilationUnit cu, AnalysisContext context,
            Set<String> addedRelationships) {

        // Buscar todas las llamadas a métodos estáticos
        cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
            if (methodCall.getScope().isPresent()) {
                String scope = methodCall.getScope().get().toString();

                // Detectar llamadas estáticas (ClassName.method())
                if (scope.contains(".") && Character.isUpperCase(scope.charAt(0))) {
                    // Es una llamada estática (ej: LoggerFactory.getLogger, Arrays.asList)
                    String staticClassName = extractSimpleClassName(scope.substring(0, scope.indexOf(".")));

                    if (isValidClassForRelation(staticClassName, context)) {
                        // Encontrar la clase que hace la llamada
                        String callerClass = findCallerClassForMethodCall(methodCall, cu);
                        if (callerClass != null && !callerClass.equals(staticClassName)) {
                            addRelationship(addedRelationships, context,
                                    callerClass + " ..> " + staticClassName + " : <<static-call>>",
                                    "Llamada estática");
                        }
                    }
                } else if (Character.isUpperCase(scope.charAt(0)) && !scope.contains(".")) {
                    // Llamada estática simple (ej: ClassName.method())
                    String staticClassName = extractSimpleClassName(scope);

                    if (isValidClassForRelation(staticClassName, context)) {
                        String callerClass = findCallerClassForMethodCall(methodCall, cu);
                        if (callerClass != null && !callerClass.equals(staticClassName)) {
                            addRelationship(addedRelationships, context,
                                    callerClass + " ..> " + staticClassName + " : <<static-call>>",
                                    "Llamada estática simple");
                        }
                    }
                }
            }
        });
    }

    /**
     * Encuentra la clase que contiene una llamada a método específica.
     */
    private String findCallerClassForMethodCall(MethodCallExpr methodCall, CompilationUnit cu) {
        // Buscar la clase que contiene este método call
        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            if (classDecl.findAll(MethodCallExpr.class).contains(methodCall)) {
                return classDecl.getNameAsString();
            }
        }
        return null;
    }

    /**
     * Analiza relaciones por manejo de excepciones (throws, catch).
     */
    private void analyzeExceptionRelationships(CompilationUnit cu, AnalysisContext context,
            Set<String> addedRelationships) {

        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = classDecl.getNameAsString();

            if (isJavaNativeClass(className)) {
                continue;
            }

            // 1. Análisis de cláusulas throws en métodos
            classDecl.getMethods().forEach(method -> {
                method.getThrownExceptions().forEach(thrownException -> {
                    String exceptionType = extractSimpleClassName(thrownException.asString());
                    if (isValidClassForRelation(exceptionType, context)) {
                        addRelationship(addedRelationships, context,
                                className + " ..> " + exceptionType + " : <<throws>>",
                                "Declara excepción");
                    }
                });
            });

            // 2. Análisis de cláusulas throws en constructores
            classDecl.getConstructors().forEach(constructor -> {
                constructor.getThrownExceptions().forEach(thrownException -> {
                    String exceptionType = extractSimpleClassName(thrownException.asString());
                    if (isValidClassForRelation(exceptionType, context)) {
                        addRelationship(addedRelationships, context,
                                className + " ..> " + exceptionType + " : <<throws>>",
                                "Constructor declara excepción");
                    }
                });
            });

            // 3. Análisis de bloques catch
            classDecl.findAll(CatchClause.class).forEach(catchClause -> {
                String exceptionType = extractSimpleClassName(catchClause.getParameter().getType().asString());
                if (isValidClassForRelation(exceptionType, context)) {
                    addRelationship(addedRelationships, context,
                            className + " ..> " + exceptionType + " : <<catches>>",
                            "Captura excepción");
                }
            });
        }
    }

    /**
     * Analiza relaciones por tipos genéricos (generics) en campos, métodos y
     * parámetros.
     */
    private void analyzeGenericTypeRelationships(CompilationUnit cu, AnalysisContext context,
            Set<String> addedRelationships) {

        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = classDecl.getNameAsString();

            if (isJavaNativeClass(className)) {
                continue;
            }

            // 1. Análisis de tipos genéricos en campos
            classDecl.getFields().forEach(field -> {
                analyzeGenericTypesInType(field.getElementType(), className, context, addedRelationships,
                        "Campo genérico");
            });

            // 2. Análisis de tipos genéricos en métodos
            classDecl.getMethods().forEach(method -> {
                // Tipo de retorno genérico
                analyzeGenericTypesInType(method.getType(), className, context, addedRelationships, "Retorno genérico");

                // Parámetros con tipos genéricos
                method.getParameters().forEach(param -> {
                    analyzeGenericTypesInType(param.getType(), className, context, addedRelationships,
                            "Parámetro genérico");
                });
            });

            // 3. Análisis de tipos genéricos en constructores
            classDecl.getConstructors().forEach(constructor -> {
                constructor.getParameters().forEach(param -> {
                    analyzeGenericTypesInType(param.getType(), className, context, addedRelationships,
                            "Constructor genérico");
                });
            });
        }
    }

    /**
     * Analiza tipos genéricos específicos en un Type y extrae relaciones.
     */
    private void analyzeGenericTypesInType(Type type, String className, AnalysisContext context,
            Set<String> addedRelationships, String relationshipDescription) {
        String typeString = type.asString();

        // Detectar tipos genéricos (ej: List<User>, Map<String, User>)
        if (typeString.contains("<") && typeString.contains(">")) {
            // Extraer tipos dentro de los genéricos
            String genericContent = typeString.substring(typeString.indexOf("<") + 1, typeString.lastIndexOf(">"));

            // Manejar múltiples tipos genéricos separados por coma
            String[] genericTypes = genericContent.split(",");

            for (String genericType : genericTypes) {
                String cleanGenericType = extractSimpleClassName(genericType.trim());
                if (isValidClassForRelation(cleanGenericType, context)) {
                    addRelationship(addedRelationships, context,
                            className + " ..> " + cleanGenericType + " : <<generic>>",
                            relationshipDescription);
                }

                // Manejar genéricos anidados (ej: List<Map<String, User>>)
                if (genericType.contains("<")) {
                    analyzeNestedGenerics(genericType.trim(), className, context, addedRelationships,
                            relationshipDescription);
                }
            }
        }
    }

    /**
     * Analiza tipos genéricos anidados recursivamente.
     */
    private void analyzeNestedGenerics(String genericType, String className, AnalysisContext context,
            Set<String> addedRelationships, String relationshipDescription) {
        if (genericType.contains("<") && genericType.contains(">")) {
            String nestedContent = genericType.substring(genericType.indexOf("<") + 1, genericType.lastIndexOf(">"));
            String[] nestedTypes = nestedContent.split(",");

            for (String nestedType : nestedTypes) {
                String cleanNestedType = extractSimpleClassName(nestedType.trim());
                if (isValidClassForRelation(cleanNestedType, context)) {
                    addRelationship(addedRelationships, context,
                            className + " ..> " + cleanNestedType + " : <<nested-generic>>",
                            relationshipDescription + " anidado");
                }

                // Recursión para genéricos más profundos
                if (nestedType.contains("<")) {
                    analyzeNestedGenerics(nestedType.trim(), className, context, addedRelationships,
                            relationshipDescription);
                }
            }
        }
    }

    /**
     * Extrae todos los nombres de clases del contexto para análisis de relaciones.
     */
    private Set<String> extractAllClassNamesFromContext(AnalysisContext context) {
        Set<String> classNames = new HashSet<>();
        for (String classDef : context.getClasses()) {
            // Extraer nombre de clase de la primera línea de definición
            String[] lines = classDef.split("\n");
            if (lines.length > 0) {
                String firstLine = lines[0].trim();
                if (firstLine.startsWith("class ") || firstLine.startsWith("interface ")) {
                    String className = firstLine.replaceFirst("(class|interface)\\s+", "")
                            .replaceAll("\\s*<<.*?>>.*", "")
                            .replaceAll("\\s*\\{.*", "")
                            .trim();
                    if (!className.isEmpty()) {
                        classNames.add(className);
                    }
                }
            }
        }
        return classNames;
    }

    /**
     * Determina el tipo de relación más apropiado para un campo basado en su
     * contexto.
     */
    private String determineFieldRelationshipType(FieldDeclaration field, ClassOrInterfaceDeclaration classDecl,
            String fieldType) {
        // Verificar si es composición (se crea en constructor)
        boolean isComposition = classDecl.getConstructors().stream()
                .anyMatch(constructor -> constructor.getBody().findAll(ObjectCreationExpr.class).stream()
                        .anyMatch(creation -> extractSimpleClassName(creation.getType().asString()).equals(fieldType)));

        // Verificar si es colección
        String fieldTypeString = field.getElementType().asString();
        boolean isCollection = fieldTypeString.contains("List") || fieldTypeString.contains("Set") ||
                fieldTypeString.contains("Collection") || fieldTypeString.contains("[]");

        // Verificar anotaciones de Spring para inyección
        boolean isInjected = field.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getNameAsString().equals("Autowired") ||
                        annotation.getNameAsString().equals("Inject"));

        if (isInjected) {
            return "..> : <<autowired>>";
        } else if (isComposition) {
            return isCollection ? "*-- \"*\"" : "*--";
        } else if (isCollection) {
            return "o-- \"*\"";
        } else {
            return "-->";
        }
    }

    /**
     * Analiza los imports para detectar dependencias del proyecto.
     */
    private void analyzeImportsForRelationships(CompilationUnit cu, String className,
            AnalysisContext context, Set<String> addedRelationships) {
        cu.getImports().forEach(importDecl -> {
            String importName = importDecl.getNameAsString();

            // Solo procesar imports del mismo proyecto (no java.*, javax.*,
            // org.springframework.*, etc.)
            if (!importName.startsWith("java.") &&
                    !importName.startsWith("javax.") &&
                    !importName.startsWith("org.springframework.") &&
                    !importName.startsWith("org.slf4j.")) {

                String importedClassName = extractSimpleClassName(importName);
                if (isValidClassForRelation(importedClassName, context)) {
                    String relationship = className + " ..> " + importedClassName + " : <<import>>";
                    if (addedRelationships.add(relationship)) {
                        context.getRelationships().add(relationship);
                        logger.debug("Dependencia de import detectada: {}", relationship);
                    }
                }
            }
        });
    }

    /**
     * Valida si una clase es válida para crear relaciones.
     */
    private boolean isValidClassForRelation(String className, AnalysisContext context) {
        return className != null &&
                !className.trim().isEmpty() &&
                !isJavaNativeClass(className) &&
                isClassInDirectory(className, context);
    }

    /**
     * Extrae el nombre simple de una clase desde un tipo complejo.
     * Maneja tipos genéricos, arrays, nombres completos de paquetes, etc.
     * 
     * @param fullType El tipo completo (ej: "java.util.List<com.example.User>",
     *                 "User[]", "com.example.User")
     * @return El nombre simple de la clase principal (ej: "List", "User", "User")
     */
    private static String extractSimpleClassName(String fullType) {
        if (fullType == null || fullType.trim().isEmpty()) {
            return "";
        }

        String cleanType = fullType.trim();

        // Manejar arrays (User[] -> User)
        if (cleanType.endsWith("[]")) {
            cleanType = cleanType.substring(0, cleanType.length() - 2);
        }

        // Manejar tipos genéricos (List<User> -> List)
        if (cleanType.contains("<")) {
            cleanType = cleanType.substring(0, cleanType.indexOf("<"));
        }

        // Manejar nombres completos de paquetes (com.example.User -> User)
        if (cleanType.contains(".")) {
            cleanType = cleanType.substring(cleanType.lastIndexOf(".") + 1);
        }

        return cleanType.trim();
    }

    private static boolean isJavaNativeClass(String className) {
        return JAVA_NATIVE_CLASSES.contains(className);
    }

    /**
     * Verifica si una clase está en el directorio de origen o sus subdirectorios.
     * 
     * @param className El nombre de la clase a verificar
     * @param context   Contexto de análisis con el path base
     * @return true si la clase está en el directorio, false en caso contrario
     */
    private boolean isClassInDirectory(String className, AnalysisContext context) {
        File srcFolder = new File(context.getBasePath());
        File[] files = srcFolder.listFiles();
        if (files == null)
            return false;

        for (File file : files) {
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
     * @param folder    directorio a revisar
     * @param className nombre de la clase a buscar
     * @return true si la clase está en el directorio, false en caso contrario
     */
    private boolean isClassInDirectory(File folder, String className) {
        File[] files = folder.listFiles();
        if (files == null)
            return false;

        for (File file : files) {
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
     * @param context Contexto de análisis con las clases y relaciones
     * @throws UMLGenerationException Si ocurre un error durante la generación del
     *                                archivo
     */
    private void generatePlantUML(AnalysisContext context) throws UMLGenerationException {
        try {
            File outputDir = new File(properties.getFullOutputPath());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            try (FileWriter writer = new FileWriter(new File(outputDir, properties.getPlantUmlFileName()))) {
                writer.write("@startuml " + properties.getDiagramFileName() + "\n");
                createSkin(writer);
                for (String clazz : context.getClasses())
                    writer.write(clazz + "\n");
                for (String rel : context.getRelationships())
                    writer.write(rel + "\n");
                writer.write("@enduml\n");
            }
            logger.info("Diagrama PlantUML generado exitosamente: {}/{}",
                    properties.getOutputDirectory(), properties.getPlantUmlFileName());
        } catch (IOException e) {
            throw new UMLGenerationException("Error al escribir archivo PlantUML",
                    "generatePlantUML", properties.getPlantUmlFileName(), e);
        }
    }

    /**
     * Genera una imagen SVG a partir del archivo PlantUML.
     * 
     * @throws PlantUMLExecutionException Si ocurre un error durante la generación
     *                                    de la imagen
     */
    private void generateImageFromPlantUML() throws PlantUMLExecutionException {
        logger.debug("Iniciando generación de imagen SVG desde archivo PlantUML");

        try {
            String plantUmlJar = properties.getResolvedPlantUmlJarPath();
            logger.debug("Usando PlantUML JAR: {}", plantUmlJar);

            String inputFile = properties.getOutputDirectory() + "/" + properties.getPlantUmlFileName();
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    plantUmlJar,
                    "-tsvg",
                    inputFile);

            String command = String.join(" ", processBuilder.command());
            logger.debug("Ejecutando comando PlantUML: {}", command);

            processBuilder.inheritIO();
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Imagen SVG generada exitosamente: {}/{}",
                        properties.getOutputDirectory(), properties.getSvgFileName());
            } else {
                throw new PlantUMLExecutionException(
                        "PlantUML terminó con código de error", command, exitCode);
            }
        } catch (IOException e) {
            throw new PlantUMLExecutionException(
                    "Error al ejecutar proceso PlantUML: " + e.getMessage(),
                    "java -jar PlantUML", -1, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PlantUMLExecutionException(
                    "Proceso PlantUML interrumpido", "java -jar PlantUML", -1, e);
        }
    }

    /**
     * Analiza relaciones por sobrescritura de métodos (@Override).
     */
    private void analyzeMethodOverrideRelationships(CompilationUnit cu, AnalysisContext context,
            Set<String> addedRelationships) {
        logger.debug("Analizando relaciones por sobrescritura de métodos (@Override)");

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getAnnotationByName("Override").isPresent()) {
                String className = getCurrentClassName(method);
                if (className != null) {
                    // Buscar clase padre que define el método original
                    method.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(classDecl -> {
                        classDecl.getExtendedTypes().forEach(extendedType -> {
                            String parentClassName = extractSimpleClassName(extendedType.asString());
                            if (isValidClassForRelation(parentClassName, context)) {
                                addRelationship(addedRelationships, context,
                                        className + " ..> " + parentClassName + " : <<overrides "
                                                + method.getNameAsString() + ">>",
                                        "Sobrescritura de método");
                            }
                        });

                        classDecl.getImplementedTypes().forEach(implType -> {
                            String interfaceName = extractSimpleClassName(implType.asString());
                            if (isValidClassForRelation(interfaceName, context)) {
                                addRelationship(addedRelationships, context,
                                        className + " ..> " + interfaceName + " : <<implements "
                                                + method.getNameAsString() + ">>",
                                        "Implementación de método");
                            }
                        });
                    });
                }
            }
        });
    }

    /**
     * Analiza relaciones por programación funcional (Lambda, Method References,
     * Stream API).
     */
    private void analyzeFunctionalProgrammingRelationships(CompilationUnit cu, AnalysisContext context,
            Set<String> addedRelationships) {
        logger.debug("Analizando relaciones por programación funcional");

        // Analizar expresiones lambda
        cu.findAll(LambdaExpr.class).forEach(lambda -> {
            String className = getCurrentClassNameFromNode(lambda);
            if (className != null) {
                addRelationship(addedRelationships, context,
                        className + " ..> \"Functional Interface\" : <<lambda>>",
                        "Expresión Lambda");
            }
        });

        // Analizar method references
        cu.findAll(MethodReferenceExpr.class).forEach(methodRef -> {
            String className = getCurrentClassNameFromNode(methodRef);
            String scope = methodRef.getScope().toString();
            String referencedClass = extractSimpleClassName(scope);

            if (className != null && isValidClassForRelation(referencedClass, context)) {
                addRelationship(addedRelationships, context,
                        className + " ..> " + referencedClass + " : <<method reference>>",
                        "Method Reference");
            }
        });

        // Analizar Stream API
        cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
            if ("stream".equals(methodCall.getNameAsString()) ||
                    "parallelStream".equals(methodCall.getNameAsString()) ||
                    methodCall.getNameAsString().matches("collect|filter|map|flatMap|reduce|forEach")) {

                String className = getCurrentClassNameFromNode(methodCall);
                if (className != null) {
                    addRelationship(addedRelationships, context,
                            className + " ..> \"Stream API\" : <<" + methodCall.getNameAsString() + ">>",
                            "Stream API");
                }
            }
        });
    }

    /**
     * Analiza relaciones por type inference (var keyword).
     */
    private void analyzeTypeInferenceRelationships(CompilationUnit cu, AnalysisContext context,
            Set<String> addedRelationships) {
        logger.debug("Analizando relaciones por type inference (var)");

        cu.findAll(VariableDeclarator.class).forEach(varDecl -> {
            if (varDecl.getType().isVarType()) {
                String className = getCurrentClassNameFromNode(varDecl);
                if (className != null && varDecl.getInitializer().isPresent()) {
                    String inferredType = extractTypeFromExpression(
                            varDecl.getInitializer().get().toString(), cu);

                    if (isValidClassForRelation(inferredType, context)) {
                        addRelationship(addedRelationships, context,
                                className + " ..> " + inferredType + " : <<var inference>>",
                                "Type Inference");
                    }
                }
            }
        });
    }

    /**
     * Analiza relaciones por interfaces funcionales (Supplier, Consumer, Function,
     * Predicate).
     */
    private void analyzeFunctionalInterfaceRelationships(CompilationUnit cu, AnalysisContext context,
            Set<String> addedRelationships) {
        logger.debug("Analizando relaciones por interfaces funcionales");

        Set<String> functionalInterfaces = Set.of("Supplier", "Consumer", "Function", "Predicate",
                "BiFunction", "BiConsumer", "BiPredicate", "UnaryOperator", "BinaryOperator");

        cu.findAll(FieldDeclaration.class).forEach(field -> {
            String fieldType = extractSimpleClassName(field.getElementType().asString());

            if (functionalInterfaces.contains(fieldType)) {
                String className = getCurrentClassNameFromNode(field);
                if (className != null) {
                    addRelationship(addedRelationships, context,
                            className + " ..> " + fieldType + " : <<functional interface>>",
                            "Interface Funcional");
                }
            }
        });

        cu.findAll(VariableDeclarator.class).forEach(varDecl -> {
            String varType = extractSimpleClassName(varDecl.getType().asString());

            if (functionalInterfaces.contains(varType)) {
                String className = getCurrentClassNameFromNode(varDecl);
                if (className != null) {
                    addRelationship(addedRelationships, context,
                            className + " ..> " + varType + " : <<functional interface>>",
                            "Interface Funcional");
                }
            }
        });

        // Analizar Optional
        cu.findAll(FieldDeclaration.class).forEach(field -> {
            if (field.getElementType().asString().startsWith("Optional")) {
                String className = getCurrentClassNameFromNode(field);
                if (className != null) {
                    addRelationship(addedRelationships, context,
                            className + " ..> Optional : <<optional>>",
                            "Optional");
                }
            }
        });
    }

    /**
     * Obtiene el nombre de la clase actual desde cualquier nodo del AST.
     */
    private String getCurrentClassNameFromNode(Node node) {
        return node.findAncestor(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse(null);
    }

    /**
     * Obtiene el nombre de la clase actual desde un método.
     */
    private String getCurrentClassName(MethodDeclaration method) {
        return method.findAncestor(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse(null);
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