package dev.akarah.jvm2df.tree.df.handler.statics;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class ForceReturnHandler implements InvokeHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.classEntry().asInternalName().equals("df/internal/CodeBlocks")
                && invoke.outline().name().equals("ret")) {
            return Optional.of(transformer -> {
                transformer.builder().locals()
                        .setResultAndReturn(
                                transformer.convertCodeTree(invoke.args().getFirst())
                        );
                return null;
            });
        }
        return Optional.empty();
    }
}
