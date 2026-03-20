package dev.akarah.jvm2df.tree.df.strategy.local;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.ParameterItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.pipeline.Pipeline;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.df.strategy.global.GlobalMemoryStrategy;

import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;

public class LineVarLocals implements LocalMemoryStrategy {
    CodeLineBuilder transformer;
    GlobalMemoryStrategy globals;

    @Override
    public void setup(CodeLineBuilder transformer, GlobalMemoryStrategy globals) {
        this.transformer = transformer;
        this.globals = globals;
    }

    @Override
    public VariableItem referenceLocal(int index) {
        return VarPattern.local(index);
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
    @SuppressWarnings("unchecked")
    public List<VarItem<?>> functionHeadParams(MethodModel methodMeta) {
        var params = new ArrayList<ParameterItem>();
        int idx = 0;

        if (!methodMeta.flags().has(AccessFlag.STATIC)) {
            idx += 1;
            var localVar = this.referenceLocal(0);
            var paramType = "any";
            params.add(new ParameterItem(localVar.name(), paramType, false, false));
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

    @Override
    public List<CodeLine> codeLineContributions(Pipeline pipeline) {
        return List.of();
    }
}
