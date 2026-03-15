package dev.akarah.jvm2df.frontend;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.codeclient.CodeClientAPI;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.tree.cfg.BytecodeTranslator;
import dev.akarah.jvm2df.tree.cfr.dom.DominanceFlowTransformer;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.df.strategy.global.BasicHeapStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LineVarLocals;
import dev.akarah.jvm2df.tree.instructions.WithContext;
import dev.akarah.jvm2df.util.Beep;

import javax.sound.sampled.LineUnavailableException;
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
        var pipeline = new Pipeline()
                .setJarPath(path)
                .setFlowTransformer(new DominanceFlowTransformer())
                .setLocalMemoryStrategy(new LineVarLocals())
                .setGlobalMemoryStrategy(new BasicHeapStrategy());
        var codeLines = pipeline.execute();
        try {
            System.out.println("=== CC STARTS BELOW");
            var cc = new CodeClientAPI(codeLines);
            cc.run();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
