package dev.akarah.jvm2df;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.codeclient.CodeClientAPI;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.tree.cfg.BytecodeTranslator;
import dev.akarah.jvm2df.tree.cfr.NaiveFlowTransformer;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.MethodMeta;
import dev.akarah.jvm2df.tree.instructions.WithContext;
import dev.akarah.jvm2df.util.Beep;

import javax.sound.sampled.LineUnavailableException;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.reflect.AccessFlag;
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
                            methodElements.methodTypeSymbol(),
                            methodElements.flags().has(AccessFlag.STATIC),
                            classElements.superclass().map(ClassEntry::asInternalName).orElse("java/lang/Object")
                    );


                    var base = new WithContext<>(
                            new BytecodeTranslator(codeModel),
                            methodMeta
                    );
                    System.out.println(base.context());
                    System.out.println(codeModel.toDebugString());
                    try {
                        var out = base
                                .map(BytecodeTranslator::split)
                                .inspect((v, c) -> System.out.println(v))
                                .map(NaiveFlowTransformer::new)
                                .map(NaiveFlowTransformer::convert)
                                .map(CodeBlockTransformer::new)
                                .map(CodeBlockTransformer::transform);
                        codeLines.addAll(out.value());
                    } catch (Exception e) {
                        try {
                            Beep.tone(330, 100, 0.5);
                        } catch (LineUnavailableException _) {
                            // ignore it, sound isn't necessary
                        }
                        throw new RuntimeException(e);
                    }
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
