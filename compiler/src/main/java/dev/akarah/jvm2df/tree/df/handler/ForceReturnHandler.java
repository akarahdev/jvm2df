package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class ForceReturnHandler implements InvokeHandler {
    @Override
    public Optional<Function<CodeBlockTransformer, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.descriptor().owner().asInternalName().equals("diamondfire/internal/CodeBlocks")
                && invoke.descriptor().name().equalsString("ret")) {
            return Optional.of(transformer -> {
                transformer.localMemoryStrategy()
                        .setResultAndReturn(
                                transformer.convertCodeTree(invoke.args().getFirst())
                        );
                return null;
            });
        }
        return Optional.empty();
    }
}
