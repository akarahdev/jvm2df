package dev.akarah.jvm2df.tree.df.handler.statics;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ArrayCloneHandler implements InvokeHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.classEntry().asSymbol().descriptorString().startsWith("[")
                && invoke.outline().name().equals("clone")) {
            return Optional.of(t -> {
                var random = VarPattern.temporary("arrayclone");
                t.builder().appendCodeBlock(ActionBlock.callFunction(
                        "globals::arrayclone",
                        List.of(
                                random,
                                t.convertCodeTree(invoke.args().getFirst())
                        )
                ));
                return random;
            });
        }
        return Optional.empty();
    }
}
