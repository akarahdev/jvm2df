package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.items.BlockTagItem;
import dev.akarah.jvm2df.codetemplate.items.GameValueItem;
import dev.akarah.jvm2df.codetemplate.items.VanillaItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.function.Function;

public class VarItemGenHandler implements InvokeHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("tag")) {
            return Optional.of(transformer -> {
                var tag = ((CodeTree.Constant) invoke.args().get(0)).constantDesc().toString();
                var option = ((CodeTree.Constant) invoke.args().get(1)).constantDesc().toString();
                return new BlockTagItem(option, tag, "?", "?");
            });
        }

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("lineVar")) {
            return Optional.of(transformer -> {
                return VarPattern.temporary("std");
            });
        }

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("gameValue")) {
            return Optional.of(transformer -> {
                var value = ((CodeTree.Constant) invoke.args().get(0)).constantDesc().toString();
                var target = ((CodeTree.Constant) invoke.args().get(1)).constantDesc().toString();
                return new GameValueItem(value, target);
            });
        }

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("vanillaItem")) {
            return Optional.of(transformer -> {
                var value = ((CodeTree.Constant) invoke.args().getFirst()).constantDesc().toString();
                return new VanillaItem(value);
            });
        }
        return Optional.empty();
    }
}
