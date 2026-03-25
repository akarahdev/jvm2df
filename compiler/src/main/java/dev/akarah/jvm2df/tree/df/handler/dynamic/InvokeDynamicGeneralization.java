package dev.akarah.jvm2df.tree.df.handler.dynamic;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class InvokeDynamicGeneralization implements InvokeDynamicHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.InvokeDynamic invokeDynamic) {
        throw new RuntimeException("I'm not sure how to handle this: " + invokeDynamic);
    }
}
