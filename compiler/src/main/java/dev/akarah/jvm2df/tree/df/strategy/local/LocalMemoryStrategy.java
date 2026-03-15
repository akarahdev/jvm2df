package dev.akarah.jvm2df.tree.df.strategy.local;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;

import java.lang.classfile.MethodModel;
import java.util.List;

public interface LocalMemoryStrategy {
    VariableItem referenceLocal(int index);
    void setResultAndReturn(VarItem<?> result);
    void compileSubroutineHint(ReconstructedFlow.SubroutineSafeHint hint);
    List<VarItem<?>> functionHeadParams(MethodModel methodModel);
    List<VarItem<?>> functionCallParams(List<VarItem<?>> parameters);
}
