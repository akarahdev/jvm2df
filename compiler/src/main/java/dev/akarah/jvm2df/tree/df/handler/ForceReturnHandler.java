package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.tree.df.FlowToDF;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class ForceReturnHandler implements InvokeHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/CodeBlocks")
                && invoke.outline().name().equals("ret")) {
            return Optional.of(transformer -> {
                transformer.builder().localMemoryStrategy()
                        .setResultAndReturn(
                                transformer.convertCodeTree(invoke.args().getFirst())
                        );
                return null;
            });
        }
        return Optional.empty();
    }
}
