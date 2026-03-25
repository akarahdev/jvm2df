package dev.akarah.jvm2df.tree.df.handler.statics;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.*;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.List;
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

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("readField")) {
            return Optional.of(transformer -> {
                var value = transformer.convertCodeTree(invoke.args().getFirst());
                var field = transformer.convertCodeTree(invoke.args().get(1));
                return transformer.builder().globals().readField(value, field);
            });
        }

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("classHandle")) {
            return Optional.of(transformer -> {
                var value = transformer.convertCodeTree(invoke.args().getFirst());
                if (value instanceof VariableItem variableItem) {
                    return VarPattern.classInfo("%var(" + variableItem.name() + ")");
                } else {
                    throw new RuntimeException("Can not grab the class of an Inline Value object");
                }
            });
        }

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("runGc")) {
            return Optional.of(transformer -> {
                transformer.builder().appendCodeBlock(ActionBlock.callFunction(
                        VarPattern.gcFunc(),
                        List.of()
                ));
                return LiteralItem.number("0");
            });
        }


        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("gcAllocationCount")) {
            return Optional.of(transformer -> {
                var variable = new VariableItem("len", "line");
                transformer.builder().appendCodeBlock(ActionBlock.setVar(
                        "GetDictSize",
                        Args.byVarItems(variable, VarPattern.gcAllocations())
                ));
                return variable;
            });
        }

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("emptySound")) {
            return Optional.of(transformer -> new SoundItem(1, 2, "Pling"));
        }

        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/VarItemGen")
                && invoke.outline().name().equals("emptyParticle")) {
            return Optional.of(transformer -> new ParticleItem(
                    "Rain",
                    new ParticleItem.Cluster(1, 0.0, 0.0),
                    new ParticleItem.Data()
            ));
        }
        return Optional.empty();
    }
}
