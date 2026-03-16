package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.items.*;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class VarItemGenHandler implements InvokeHandler {
    @Override
    public Optional<Function<CodeBlockTransformer, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.descriptor().owner().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.descriptor().name().equalsString("tag")) {
            return Optional.of(transformer -> {
                var tag = ((CodeTree.Constant) invoke.args().get(0)).constantDesc().toString();
                var option = ((CodeTree.Constant) invoke.args().get(1)).constantDesc().toString();
                return new BlockTagItem(option, tag, "?", "?");
            });
        }

        if (invoke.descriptor().owner().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.descriptor().name().equalsString("lineVar")) {
            return Optional.of(transformer -> {
                return new VariableItem("tmp.std." + new Object().hashCode(), "line");
            });
        }

        if (invoke.descriptor().owner().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.descriptor().name().equalsString("gameValue")) {
            return Optional.of(transformer -> {
                var value = ((CodeTree.Constant) invoke.args().get(0)).constantDesc().toString();
                var target = ((CodeTree.Constant) invoke.args().get(1)).constantDesc().toString();
                return new GameValueItem(value, target);
            });
        }

        if (invoke.descriptor().owner().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.descriptor().name().equalsString("vanillaItem")) {
            return Optional.of(transformer -> {
                var value = ((CodeTree.Constant) invoke.args().getFirst()).constantDesc().toString();
                return new VanillaItem(value);
            });
        }
        return Optional.empty();
    }
}
