package dev.akarah.jvm2df;

import dev.akarah.jvm2df.bytecode.JarToClasses;

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
    }
}
