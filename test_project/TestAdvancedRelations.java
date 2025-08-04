package test.advanced;

import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Optional;
import java.util.List;
import java.util.stream.Stream;

public class TestAdvancedRelations {
    
    // Interfaces funcionales
    private Supplier<String> stringSupplier;
    private Consumer<String> stringConsumer;
    private Function<String, Integer> stringToIntFunction;
    private Predicate<String> stringPredicate;
    
    // Optional
    private Optional<String> optionalString;
    
    // Método con @Override (simulado)
    @Override
    public String toString() {
        return "TestAdvancedRelations";
    }
    
    // Método que usa var (type inference)
    public void testVarInference() {
        var list = List.of("hello", "world");
        var stream = list.stream();
        var result = stream.filter(s -> s.length() > 5).findFirst();
    }
    
    // Método que usa lambda expressions
    public void testLambdaExpressions() {
        List<String> strings = List.of("a", "bb", "ccc");
        
        // Lambda con filter
        strings.stream()
               .filter(s -> s.length() > 1)
               .forEach(System.out::println);
        
        // Lambda con map
        strings.stream()
               .map(String::toUpperCase)
               .collect(java.util.stream.Collectors.toList());
    }
    
    // Método que usa method references
    public void testMethodReferences() {
        List<String> strings = List.of("hello", "world");
        
        // Method reference estático
        strings.stream()
               .map(String::toUpperCase)
               .forEach(System.out::println);
        
        // Method reference de instancia
        strings.forEach(this::processString);
    }
    
    // Método que usa Stream API intensivamente
    public void testStreamAPI() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        
        numbers.stream()
               .filter(n -> n % 2 == 0)
               .map(n -> n * 2)
               .reduce(0, Integer::sum);
        
        numbers.parallelStream()
               .collect(java.util.stream.Collectors.groupingBy(n -> n % 2));
    }
    
    private void processString(String s) {
        System.out.println("Processing: " + s);
    }
}

class BaseClass {
    public void baseMethod() {
        System.out.println("Base method");
    }
}

class DerivedClass extends BaseClass {
    @Override
    public void baseMethod() {
        super.baseMethod();
        System.out.println("Derived method");
    }
}
