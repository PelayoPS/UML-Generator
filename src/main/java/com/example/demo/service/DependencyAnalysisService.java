package com.example.demo.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para análisis de dependencias externas basado en imports.
 * Detecta frameworks, librerías y patrones arquitectónicos.
 * 
 * @author @PelayoPS
 */
@Service
public class DependencyAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(DependencyAnalysisService.class);
    
    // Categorías de frameworks conocidos
    private static final Map<String, String> FRAMEWORK_CATEGORIES = Map.of(
        "org.springframework", "Spring Framework",
        "org.hibernate", "Hibernate ORM", 
        "javax.persistence", "JPA",
        "jakarta.persistence", "Jakarta JPA",
        "com.fasterxml.jackson", "Jackson JSON",
        "org.slf4j", "SLF4J Logging",
        "org.apache.commons", "Apache Commons",
        "org.junit", "JUnit Testing",
        "org.mockito", "Mockito Testing",
        "io.swagger", "Swagger/OpenAPI"
    );
    
    /**
     * Analiza todas las dependencias externas de una unidad de compilación.
     * 
     * @param cu Unidad de compilación a analizar
     * @return Información de dependencias organizadas por categoría
     */
    public DependencyReport analyzeDependencies(CompilationUnit cu) {
        DependencyReport report = new DependencyReport();
        
        List<ImportDeclaration> imports = cu.getImports();
        
        for (ImportDeclaration importDecl : imports) {
            String importName = importDecl.getNameAsString();
            String category = categorizeImport(importName);
            
            report.addDependency(category, importName);
        }
        
        // Analizar patrones específicos
        analyzeSpringPatterns(imports, report);
        analyzeJpaPatterns(imports, report);
        analyzeTestingPatterns(imports, report);
        
        logger.info("Análisis de dependencias completado: {} categorías, {} imports totales", 
            report.getCategories().size(), report.getTotalImports());
        
        return report;
    }
    
    /**
     * Categoriza un import según el framework o librería.
     * 
     * @param importName Nombre completo del import
     * @return Categoría del framework
     */
    private String categorizeImport(String importName) {
        for (Map.Entry<String, String> entry : FRAMEWORK_CATEGORIES.entrySet()) {
            if (importName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Categorías adicionales basadas en patterns
        if (importName.startsWith("java.")) {
            return "Java Core";
        } else if (importName.startsWith("javax.") || importName.startsWith("jakarta.")) {
            return "Java EE/Jakarta";
        } else if (importName.contains(".test.") || importName.contains(".testing.")) {
            return "Testing";
        } else if (importName.startsWith("com.") || importName.startsWith("org.")) {
            return "External Libraries";
        } else {
            return "Project Internal";
        }
    }
    
    /**
     * Analiza patrones específicos de Spring Framework.
     */
    private void analyzeSpringPatterns(List<ImportDeclaration> imports, DependencyReport report) {
        Set<String> springImports = imports.stream()
            .map(ImportDeclaration::getNameAsString)
            .filter(name -> name.startsWith("org.springframework"))
            .collect(Collectors.toSet());
        
        if (springImports.isEmpty()) return;
        
        List<String> patterns = new ArrayList<>();
        
        if (springImports.stream().anyMatch(imp -> imp.contains(".boot."))) {
            patterns.add("Spring Boot Application");
        }
        if (springImports.stream().anyMatch(imp -> imp.contains(".web."))) {
            patterns.add("Spring Web MVC");
        }
        if (springImports.stream().anyMatch(imp -> imp.contains(".data."))) {
            patterns.add("Spring Data");
        }
        if (springImports.stream().anyMatch(imp -> imp.contains(".security."))) {
            patterns.add("Spring Security");
        }
        if (springImports.stream().anyMatch(imp -> imp.contains(".transaction."))) {
            patterns.add("Spring Transactions");
        }
        
        report.addPattern("Spring Patterns", patterns);
    }
    
    /**
     * Analiza patrones de JPA/Hibernate.
     */
    private void analyzeJpaPatterns(List<ImportDeclaration> imports, DependencyReport report) {
        Set<String> jpaImports = imports.stream()
            .map(ImportDeclaration::getNameAsString)
            .filter(name -> name.contains("persistence") || name.contains("hibernate"))
            .collect(Collectors.toSet());
        
        if (jpaImports.isEmpty()) return;
        
        List<String> patterns = new ArrayList<>();
        
        if (jpaImports.stream().anyMatch(imp -> imp.contains("Entity"))) {
            patterns.add("JPA Entities");
        }
        if (jpaImports.stream().anyMatch(imp -> imp.contains("Repository"))) {
            patterns.add("Repository Pattern");
        }
        if (jpaImports.stream().anyMatch(imp -> imp.contains("Query"))) {
            patterns.add("Custom Queries");
        }
        
        report.addPattern("Data Access Patterns", patterns);
    }
    
    /**
     * Analiza patrones de testing.
     */
    private void analyzeTestingPatterns(List<ImportDeclaration> imports, DependencyReport report) {
        Set<String> testImports = imports.stream()
            .map(ImportDeclaration::getNameAsString)
            .filter(name -> name.contains("test") || name.contains("junit") || name.contains("mockito"))
            .collect(Collectors.toSet());
        
        if (testImports.isEmpty()) return;
        
        List<String> patterns = new ArrayList<>();
        
        if (testImports.stream().anyMatch(imp -> imp.contains("junit"))) {
            patterns.add("JUnit Testing");
        }
        if (testImports.stream().anyMatch(imp -> imp.contains("mockito"))) {
            patterns.add("Mock Testing");
        }
        if (testImports.stream().anyMatch(imp -> imp.contains("spring") && imp.contains("test"))) {
            patterns.add("Spring Integration Tests");
        }
        
        report.addPattern("Testing Patterns", patterns);
    }
    
    /**
     * Genera comentarios PlantUML con información de dependencias.
     * 
     * @param report Reporte de dependencias
     * @return Líneas de comentarios para incluir en el diagrama
     */
    public List<String> generatePlantUMLComments(DependencyReport report) {
        List<String> comments = new ArrayList<>();
        
        comments.add("' === DEPENDENCY ANALYSIS ===");
        comments.add("' Total frameworks detected: " + report.getCategories().size());
        
        for (Map.Entry<String, Set<String>> entry : report.getDependenciesByCategory().entrySet()) {
            if (!entry.getKey().equals("Java Core") && !entry.getKey().equals("Project Internal")) {
                comments.add("' " + entry.getKey() + ": " + entry.getValue().size() + " imports");
            }
        }
        
        if (!report.getPatterns().isEmpty()) {
            comments.add("' === ARCHITECTURAL PATTERNS ===");
            for (Map.Entry<String, List<String>> patternEntry : report.getPatterns().entrySet()) {
                comments.add("' " + patternEntry.getKey() + ": " + String.join(", ", patternEntry.getValue()));
            }
        }
        
        comments.add("' ===========================");
        
        return comments;
    }
    
    /**
     * Clase para almacenar el reporte de análisis de dependencias.
     */
    public static class DependencyReport {
        private final Map<String, Set<String>> dependenciesByCategory = new HashMap<>();
        private final Map<String, List<String>> patterns = new HashMap<>();
        
        public void addDependency(String category, String dependency) {
            dependenciesByCategory.computeIfAbsent(category, k -> new HashSet<>()).add(dependency);
        }
        
        public void addPattern(String patternType, List<String> patternList) {
            if (!patternList.isEmpty()) {
                patterns.put(patternType, new ArrayList<>(patternList));
            }
        }
        
        public Set<String> getCategories() {
            return dependenciesByCategory.keySet();
        }
        
        public Map<String, Set<String>> getDependenciesByCategory() {
            return Collections.unmodifiableMap(dependenciesByCategory);
        }
        
        public Map<String, List<String>> getPatterns() {
            return Collections.unmodifiableMap(patterns);
        }
        
        public int getTotalImports() {
            return dependenciesByCategory.values().stream()
                .mapToInt(Set::size)
                .sum();
        }
    }
}
