package dev.akarah.jvm2df.tree.df.strategy.local;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;

import java.lang.classfile.MethodModel;
import java.util.List;

public interface LocalMemoryStrategy {
    void setup(CodeLineBuilder transformer);

    VariableItem referenceLocal(int index);

    void setResultAndReturn(VarItem<?> result);

    List<VarItem<?>> functionHeadParams(MethodModel methodModel);

    List<VarItem<?>> functionCallParams(List<VarItem<?>> parameters);
}
