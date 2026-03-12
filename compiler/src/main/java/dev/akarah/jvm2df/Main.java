package dev.akarah.jvm2df;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.bytecode.MethodFlowAnalysis;
import dev.akarah.jvm2df.codetemplate.CodeTemplateData;
import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;

import java.nio.file.Path;
import java.util.List;

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

//      System.out.println(new MethodFlowAnalysis(code).analyze());
    }
}
