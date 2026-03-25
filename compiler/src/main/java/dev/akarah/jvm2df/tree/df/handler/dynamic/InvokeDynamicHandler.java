package dev.akarah.jvm2df.tree.df.handler.dynamic;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface InvokeDynamicHandler {
    List<InvokeDynamicHandler> INVOKE_HANDLERS = List.of(
            new HandleStringConcatFactory(),
            new InvokeDynamicGeneralization()
    );

    Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.InvokeDynamic invokeDynamic);
}
