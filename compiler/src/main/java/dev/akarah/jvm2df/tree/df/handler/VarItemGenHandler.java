package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.items.BlockTagItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class VarItemGenHandler implements InvokeHandler {
    @Override
    public Optional<Function<CodeBlockTransformer, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if(invoke.descriptor().startsWith("diamondfire/internal/VarItemGen#tag")) {
            return Optional.of(transformer -> {
                var tag = ((CodeTree.Constant) invoke.args().get(0)).constantDesc().toString();
                var option = ((CodeTree.Constant) invoke.args().get(1)).constantDesc().toString();
                var action = ((CodeTree.Constant) invoke.args().get(2)).constantDesc().toString();
                var block = ((CodeTree.Constant) invoke.args().get(3)).constantDesc().toString();
                return new BlockTagItem(option, tag, action, block);
            });
        }
        if(invoke.descriptor().startsWith("diamondfire/internal/VarItemGen#lineVar")) {
            return Optional.of(transformer -> {
                return new VariableItem("ret." + new Object().hashCode(), "line");
            });
        }
        return Optional.empty();
    }
}
