package dev.akarah.jvm2df.tree.df.strategy.local;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.ParameterItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;

import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;

public class LineVarLocals implements LocalMemoryStrategy {
    CodeBlockTransformer transformer;

    @Override
    public void setup(CodeBlockTransformer transformer) {
        this.transformer = transformer;
    }

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
        this.transformer.convertFlowBlock(hint.block());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<VarItem<?>> functionHeadParams(MethodModel methodMeta) {
        var params = new ArrayList<ParameterItem>();
        int idx = 0;
        if (!methodMeta.flags().has(AccessFlag.STATIC)) {
            idx += 1;
            var localVar = this.referenceLocal(0);
            params.add(new ParameterItem(localVar.name(), "any", false, false));
        }
        for (var parameter : methodMeta.methodTypeSymbol().parameterList()) {
            var localVar = this.referenceLocal(idx);
            params.add(new ParameterItem(localVar.name(), "any", false, false));
            idx += 1;
            if (parameter.descriptorString().equals("D") || parameter.descriptorString().equals("L")) {
                idx += 1;
            }
        }
        if (!methodMeta.methodTypeSymbol().returnType().equals(ClassDesc.ofDescriptor("V"))) {
            params.addFirst(new ParameterItem("return", "var", false, false));
        }
        return (List<VarItem<?>>) (Object) params;
    }

    @Override
    public List<VarItem<?>> functionCallParams(List<VarItem<?>> parameters) {
        return parameters;
    }
}
