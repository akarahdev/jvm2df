package dev.akarah.jvm2df;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.tree.cfg.BytecodeTranslator;
import dev.akarah.jvm2df.tree.cfr.NaiveFlowTransformer;

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
            classElements.methods().forEach(methodElements -> {
                methodElements.code().ifPresent(codeModel -> {
                    var splitter = new BytecodeTranslator(codeModel);
                    System.out.println(classElements.thisClass().name() + "#" + methodElements.methodName());
                    var blocks = splitter.split();
                    // System.out.println(blocks);

                    var transformed = new NaiveFlowTransformer().convert(blocks);
                    System.out.println(transformed);
                });
            });
        });
    }
}
