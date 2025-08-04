package com.example.demo.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para análisis de anotaciones en código Java.
 * Detecta anotaciones Spring, JPA y otras frameworks populares.
 * 
 * @author @PelayoPS
 */
@Service
public class AnnotationAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationAnalysisService.class);
    
    // Mapeo de anotaciones a estereotipos PlantUML
    private static final Map<String, String> ANNOTATION_STEREOTYPES = Map.of(
        "Entity", "<<Entity>>",
        "Service", "<<Service>>", 
        "Controller", "<<Controller>>",
        "RestController", "<<RestController>>",
        "Repository", "<<Repository>>",
        "Component", "<<Component>>",
        "Configuration", "<<Configuration>>",
        "Autowired", "<<Autowired>>",
        "Transactional", "<<Transactional>>",
        "RequestMapping", "<<Endpoint>>"
    );
    
    // Anotaciones de interés para el análisis
    private static final Set<String> IMPORTANT_ANNOTATIONS = Set.of(
        "Entity", "Table", "Service", "Controller", "RestController", 
        "Repository", "Component", "Configuration", "Autowired",
        "Transactional", "RequestMapping", "GetMapping", "PostMapping",
        "PutMapping", "DeleteMapping", "Override", "Deprecated"
    );
    
    /**
     * Analiza las anotaciones de una clase y devuelve estereotipos PlantUML.
     * 
     * @param clazz Declaración de clase a analizar
     * @return Lista de estereotipos para PlantUML
     */
    public List<String> analyzeClassAnnotations(ClassOrInterfaceDeclaration clazz) {
        List<String> stereotypes = new ArrayList<>();
        
        for (AnnotationExpr annotation : clazz.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            
            if (ANNOTATION_STEREOTYPES.containsKey(annotationName)) {
                stereotypes.add(ANNOTATION_STEREOTYPES.get(annotationName));
                logger.debug("Anotación detectada en clase {}: {}", clazz.getNameAsString(), annotationName);
            }
        }
        
        return stereotypes;
    }
    
    /**
     * Genera una línea de clase PlantUML con estereotipos de anotaciones.
     * 
     * @param clazz Declaración de clase
     * @return Línea PlantUML formateada con estereotipos
     */
    public String generateClassLineWithAnnotations(ClassOrInterfaceDeclaration clazz) {
        String className = clazz.getNameAsString();
        String classType = clazz.isInterface() ? "interface" : "class";
        List<String> stereotypes = analyzeClassAnnotations(clazz);
        
        StringBuilder classLine = new StringBuilder();
        classLine.append(classType).append(" ").append(className);
        
        if (!stereotypes.isEmpty()) {
            classLine.append(" ");
            classLine.append(String.join(" ", stereotypes));
        }
        
        classLine.append(" {");
        
        return classLine.toString();
    }
    
    /**
     * Extrae información de dependencias basada en anotaciones @Autowired.
     * 
     * @param cu Unidad de compilación a analizar
     * @return Mapa de clase -> lista de dependencias inyectadas
     */
    public Map<String, List<String>> extractDependencyInjections(CompilationUnit cu) {
        Map<String, List<String>> dependencies = new HashMap<>();
        
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            String className = clazz.getNameAsString();
            List<String> injectedDependencies = new ArrayList<>();
            
            // Analizar campos con @Autowired
            clazz.getFields().forEach(field -> {
                if (field.getAnnotations().stream()
                    .anyMatch(ann -> ann.getNameAsString().equals("Autowired"))) {
                    String fieldType = field.getVariable(0).getTypeAsString();
                    injectedDependencies.add(fieldType);
                    logger.debug("Dependencia inyectada detectada en {}: {}", className, fieldType);
                }
            });
            
            // Analizar constructores para inyección por constructor
            clazz.getConstructors().forEach(constructor -> {
                if (constructor.getParameters().size() > 0) {
                    constructor.getParameters().forEach(param -> {
                        String paramType = param.getTypeAsString();
                        // Asumir que parámetros de constructor son dependencias inyectadas
                        if (!isJavaBuiltinType(paramType)) {
                            injectedDependencies.add(paramType);
                            logger.debug("Dependencia por constructor detectada en {}: {}", className, paramType);
                        }
                    });
                }
            });
            
            if (!injectedDependencies.isEmpty()) {
                dependencies.put(className, injectedDependencies);
            }
        });
        
        return dependencies;
    }
    
    /**
     * Genera estadísticas de uso de anotaciones.
     * 
     * @param cu Unidad de compilación a analizar
     * @return Mapa con conteos de cada anotación
     */
    public Map<String, Integer> generateAnnotationStatistics(CompilationUnit cu) {
        Map<String, Integer> stats = new HashMap<>();
        
        cu.findAll(AnnotationExpr.class).forEach(annotation -> {
            String annotationName = annotation.getNameAsString();
            if (IMPORTANT_ANNOTATIONS.contains(annotationName)) {
                stats.merge(annotationName, 1, Integer::sum);
            }
        });
        
        logger.info("Estadísticas de anotaciones: {}", stats);
        return stats;
    }
    
    /**
     * Detecta patrones arquitectónicos basados en anotaciones.
     * 
     * @param cu Unidad de compilación a analizar
     * @return Lista de patrones detectados
     */
    public List<String> detectArchitecturalPatterns(CompilationUnit cu) {
        List<String> patterns = new ArrayList<>();
        Set<String> annotationsFound = new HashSet<>();
        
        cu.findAll(AnnotationExpr.class).forEach(annotation -> {
            annotationsFound.add(annotation.getNameAsString());
        });
        
        // Detectar patrón MVC
        if (annotationsFound.contains("Controller") || annotationsFound.contains("RestController")) {
            if (annotationsFound.contains("Service") && annotationsFound.contains("Repository")) {
                patterns.add("MVC Pattern");
            }
        }
        
        // Detectar patrón JPA/Hibernate
        if (annotationsFound.contains("Entity") && annotationsFound.contains("Repository")) {
            patterns.add("JPA/Data Access Pattern");
        }
        
        // Detectar patrón Spring Boot
        if (annotationsFound.contains("SpringBootApplication") || 
            (annotationsFound.contains("Configuration") && annotationsFound.contains("Autowired"))) {
            patterns.add("Spring Boot Pattern");
        }
        
        // Detectar patrón REST API
        if (annotationsFound.stream().anyMatch(ann -> 
            ann.endsWith("Mapping") || ann.equals("RestController"))) {
            patterns.add("REST API Pattern");
        }
        
        logger.info("Patrones arquitectónicos detectados: {}", patterns);
        return patterns;
    }
    
    /**
     * Verifica si un tipo es un tipo built-in de Java.
     * 
     * @param typeName Nombre del tipo
     * @return true si es un tipo built-in
     */
    private boolean isJavaBuiltinType(String typeName) {
        return typeName.equals("String") || typeName.equals("int") || typeName.equals("Integer") ||
               typeName.equals("long") || typeName.equals("Long") || typeName.equals("double") ||
               typeName.equals("Double") || typeName.equals("boolean") || typeName.equals("Boolean") ||
               typeName.equals("float") || typeName.equals("Float") || typeName.equals("char") ||
               typeName.equals("Character") || typeName.equals("byte") || typeName.equals("Byte") ||
               typeName.equals("short") || typeName.equals("Short");
    }
}
