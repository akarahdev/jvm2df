package dev.akarah.jvm2df.tree.df.strategy;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.ParameterItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.MethodMeta;

import java.util.List;
import java.util.stream.IntStream;

public record LineVarLocals(CodeBlockTransformer transformer) implements LocalMemoryStrategy {
    @Override
    public VariableItem referenceLocal(int index) {
        return new VariableItem("local." + index, "line");
    }

    @Override
    public void setResultAndReturn(VarItem<?> result) {
        transformer.appendCodeBlock(ActionBlock.setVar(
                "=",
                Args.byVarItems(
                        new VariableItem("return", "line"),
                        result
                )
        ));
        transformer.appendCodeBlock(ActionBlock.control("Return", Args.byVarItems()));
    }

    @Override
    public void compileSubroutineHint(ReconstructedFlow.SubroutineSafeHint hint) {
        this.transformer().convertFlowBlock(hint.block());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<VarItem<?>> functionHeadParams(MethodMeta methodMeta) {
        return (List<VarItem<?>>) (Object) IntStream.range(0, methodMeta.methodTypeDesc().parameterCount())
                .mapToObj(this::referenceLocal)
                .map(x -> new ParameterItem(x.name(), "any", false, false))
                .toList();
    }

    @Override
    public List<VarItem<?>> functionCallParams(List<VarItem<?>> parameters) {
        return parameters;
    }
}
