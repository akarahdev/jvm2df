package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface InvokeHandler {
    List<InvokeHandler> INVOKE_HANDLERS = List.of(
            new DFCodeBlocksHandler("control", ActionBlock::control),
            new DFCodeBlocksHandler("playerAction", ActionBlock::playerAction),
            new DFCodeBlocksHandler("setVar", ActionBlock::setVar),
            new VarItemGenHandler(),
            new BoxedPrimitiveHandler()
    );

    Optional<Function<CodeBlockTransformer, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke);
}
