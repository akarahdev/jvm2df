package dev.akarah.jvm2df.frontend;

import dev.akarah.jvm2df.codeclient.CodeClientAPI;
import dev.akarah.jvm2df.pipeline.GenerateFieldSetup;
import dev.akarah.jvm2df.pipeline.GenerateJavaMethods;
import dev.akarah.jvm2df.pipeline.Pipeline;
import dev.akarah.jvm2df.tree.cfr.dom.DominanceFlowTransformer;
import dev.akarah.jvm2df.tree.df.strategy.global.DictHeapStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LineVarLocals;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class Main {
    static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: Provide a jar file to compile.");
            return;
        }

        var path = Path.of(args[0]).toAbsolutePath();
        var pipeline = new Pipeline()
                .setJarPath(path)
                .setFlowTransformer(new DominanceFlowTransformer())
                .setLocalMemoryStrategy(new LineVarLocals())
                .setGlobalMemoryStrategy(new DictHeapStrategy())
                .registerComponent(new GenerateJavaMethods())
                .registerComponent(new GenerateFieldSetup());
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
