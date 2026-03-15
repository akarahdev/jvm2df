package dev.akarah.jvm2df.frontend;

import dev.akarah.jvm2df.codeclient.CodeClientAPI;
import dev.akarah.jvm2df.tree.cfr.dom.DominanceFlowTransformer;
import dev.akarah.jvm2df.tree.df.strategy.global.BasicHeapStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LineVarLocals;

import java.net.URISyntaxException;
import java.nio.file.Path;

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
