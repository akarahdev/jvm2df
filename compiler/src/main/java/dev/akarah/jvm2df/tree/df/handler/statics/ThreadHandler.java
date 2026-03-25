package dev.akarah.jvm2df.tree.df.handler.statics;

import dev.akarah.jvm2df.codetemplate.blocks.Bracket;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.pipeline.FlowToDF;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ThreadHandler implements InvokeHandler {
    @Override
    public Optional<Function<FlowToDF, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        if (invoke.classEntry().asInternalName().equals("java/lang/Thread")
                && invoke.outline().name().equals("start0")) {
            return Optional.of(transformer -> {
                transformer.builder().globals().invokeVirtual(
                        (VariableItem) transformer.convertCodeTree(invoke.args().getFirst()),
                        new CompilationGraph.MethodOutline(
                                "run",
                                MethodTypeDesc.of(ClassDesc.ofDescriptor("V"))
                        ),
                        List.of(
                                transformer.convertCodeTree(invoke.args().getFirst())
                        ),
                        true
                );
                return null;
            });
        }
        if (invoke.classEntry().asInternalName().equals("diamondfire/internal/CodeBlocks")
                && invoke.outline().name().equals("closeNormal")) {
            return Optional.of(transformer -> {
                transformer.builder().appendCodeBlock(Bracket.closeNormal());
                return null;
            });
        }
        return Optional.empty();
    }
}
