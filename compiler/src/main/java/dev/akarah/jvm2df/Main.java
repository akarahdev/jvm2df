package dev.akarah.jvm2df;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.codeclient.CodeClientAPI;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.tree.cfg.BytecodeTranslator;
import dev.akarah.jvm2df.tree.cfr.NaiveFlowTransformer;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.MethodMeta;
import dev.akarah.jvm2df.tree.instructions.WithContext;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;

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

        final var codeLines = new ArrayList<CodeLine>();
        classes.forEach(classElements -> {
            classElements.methods().forEach(methodElements -> {
                methodElements.code().ifPresent(codeModel -> {
                    var methodMeta = new MethodMeta(
                            classElements.thisClass().asInternalName(),
                            methodElements.methodName().stringValue(),
                            methodElements.methodTypeSymbol()
                    );


                    var base = new WithContext<>(
                            new BytecodeTranslator(codeModel),
                            methodMeta
                    );
                    var out = base.map(BytecodeTranslator::split)
                            .map(NaiveFlowTransformer::new)
                            .map(NaiveFlowTransformer::convert)
                            .map(CodeBlockTransformer::new)
                            .map(CodeBlockTransformer::transform);
                    codeLines.addAll(out.value());
                    System.out.println(out.value());
                });
            });
        });

        try {
            var cc = new CodeClientAPI(codeLines);
            cc.run();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
