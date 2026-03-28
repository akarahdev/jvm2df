package dev.akarah.jvm2df.tree.df.strategy.global;

import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.pipeline.Pipeline;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;

import java.util.List;

public interface GlobalMemoryStrategy {
    CodeLineBuilder transformer();

    LocalMemoryStrategy locals();

    void setup(CodeLineBuilder transformer, LocalMemoryStrategy locals);

    VarItem<?> allocate();

    void setField(VarItem<?> allocation, VarItem<?> field, VarItem<?> value);

    void setStaticField(String clazz, String field, VarItem<?> value);

    VarItem<?> readField(VarItem<?> allocation, VarItem<?> field);

    VarItem<?> readStaticField(String clazz, String field);

    VarItem<?> readClass(VarItem<?> allocation);

    void invokeVirtual(
            VarItem<?> callerItem,
            CompilationGraph.MethodOutline methodOutline,
            List<VarItem<?>> parameters,
            boolean seperateProcess
    );

    void reference(VarItem<?> allocation);

    void dereference(VarItem<?> allocation);

    void cleanup(int locals);

    List<CodeLine> codeLineContributions(Pipeline pipeline);
}
