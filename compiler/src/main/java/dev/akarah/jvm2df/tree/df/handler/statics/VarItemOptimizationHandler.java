package dev.akarah.jvm2df.tree.df.handler.statics;

import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VanillaItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.lang.constant.ConstantDesc;
import java.util.Optional;
import java.util.function.Function;

public class VarItemOptimizationHandler implements InvokeHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.classEntry().asInternalName().equals("df/value/Text")
                && invoke.outline().name().equals("of")
                && invoke.args().size() == 1
                && invoke.args().getFirst() instanceof CodeTree.Constant(ConstantDesc constantDesc)) {
            return Optional.of(transformer -> {
                return LiteralItem.text(constantDesc.toString());
            });
        }
        if (invoke.classEntry().asInternalName().equals("df/value/ItemStack")
                && invoke.outline().name().equals("of")
                && invoke.args().size() == 1
                && invoke.args().getFirst() instanceof CodeTree.Constant(ConstantDesc constantDesc)) {
            return Optional.of(transformer -> {
                return new VanillaItem("{DF_NBT:4671,count:1,id:'" + constantDesc.toString() + "'}");
            });
        }
        return Optional.empty();
    }
}
