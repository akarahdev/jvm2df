package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.items.BlockTagItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class BoxedPrimitiveHandler implements InvokeHandler {
    @Override
    public Optional<Function<CodeBlockTransformer, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if(invoke.descriptor().startsWith("java/lang/Integer#valueOf")
        || invoke.descriptor().startsWith("java/lang/Long#valueOf")
        || invoke.descriptor().startsWith("java/lang/Double#valueOf")
        || invoke.descriptor().startsWith("java/lang/Float#valueOf")) {
            return Optional.of(
                    transformer -> transformer.convertCodeTree(invoke.args().getFirst())
            );
        }
        return Optional.empty();
    }
}
