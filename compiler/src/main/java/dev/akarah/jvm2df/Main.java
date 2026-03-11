package dev.akarah.jvm2df;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.bytecode.MethodFlowAnalysis;

import java.nio.file.Path;

public class Main {
    static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Error: Provide a jar file to compile.");
            return;
        }

        var path = Path.of(args[0]).toAbsolutePath();
        System.out.println("Compiling " + path);
        var classes = JarToClasses.convert(path);
        System.out.println(classes);

        classes.forEach(classElements -> {
            System.out.println(classElements.thisClass().name());
            classElements.methods().forEach(methodElements -> {
                System.out.println(methodElements.methodName());
                methodElements.code().ifPresent(code -> {
                    System.out.println(code.toDebugString());
                    System.out.println(new MethodFlowAnalysis(code).analyze());
                });
            });
        });
    }
}
