package dev.akarah.jvm2df.tree.df.strategy.global;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.pipeline.Pipeline;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;

import java.util.List;

public class DictHeapStrategy implements GlobalMemoryStrategy {
    CodeLineBuilder transformer;
    LocalMemoryStrategy localMemoryStrategy;

    @Override
    public CodeLineBuilder transformer() {
        return this.transformer;
    }

    @Override
    public LocalMemoryStrategy locals() {
        return this.localMemoryStrategy;
    }

    @Override
    public void setup(CodeLineBuilder transformer, LocalMemoryStrategy locals) {
        this.transformer = transformer;
        this.localMemoryStrategy = locals;
    }

    @Override
    public VarItem<?> allocate() {
        var allocationNameVar = VarPattern.temporary("allocation");
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "=",
                Args.byVarItems(
                        allocationNameVar,
                        VarPattern.newMemoryAddress()
                )
        ));
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "CreateDict",
                Args.byVarItems(
                        new VariableItem("%var(" + allocationNameVar.name() + ")", "unsaved")
                )
        ));
        return allocationNameVar;
    }

    @Override
    public void setField(VarItem<?> allocation, VarItem<?> field, VarItem<?> value) {
        if (allocation instanceof VariableItem allocationVar) {
            if (field instanceof LiteralItem fieldLiteral) {
                this.transformer.appendCodeBlock(ActionBlock.setVar(
                        "SetDictValue",
                        Args.byVarItems(
                                new VariableItem("%var(" + allocationVar.name() + ")", "unsaved"),
                                LiteralItem.string(fieldLiteral.value()),
                                value
                        )
                ));
            } else if (field instanceof VariableItem fieldVar) {
                this.transformer.appendCodeBlock(ActionBlock.setVar(
                        "SetDictValue",
                        Args.byVarItems(
                                new VariableItem("%var(" + allocationVar.name() + ").%var(" + fieldVar.name() + ")", "unsaved"),
                                LiteralItem.string("%var(" + fieldVar.name() + ")"),
                                value
                        )
                ));
            } else {
                throw new RuntimeException("Fields must be literals or variables");
            }
        } else {
            throw new RuntimeException("Allocations must be variables");
        }
    }

    @Override
    public void setStaticField(String clazz, String field, VarItem<?> value) {
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "SetDictValue",
                Args.byVarItems(
                        new VariableItem("statics", "unsaved"),
                        LiteralItem.string(clazz + "." + field),
                        value
                )
        ));
    }

    @Override
    public VarItem<?> readField(VarItem<?> allocation, VarItem<?> field) {
        var readVar = VarPattern.temporary("read");
        if (allocation instanceof VariableItem allocationVar) {
            if (field instanceof LiteralItem literalItem) {
                this.transformer.appendCodeBlock(ActionBlock.setVar(
                        "GetDictValue",
                        Args.byVarItems(
                                readVar,
                                new VariableItem("%var(" + allocationVar.name() + ")", "unsaved"),
                                LiteralItem.string(literalItem.value())
                        )
                ));
            } else if (field instanceof VariableItem variableItem) {
                this.transformer.appendCodeBlock(ActionBlock.setVar(
                        "GetDictValue",
                        Args.byVarItems(
                                readVar,
                                new VariableItem("%var(" + allocationVar.name() + ")", "unsaved"),
                                LiteralItem.string("%var(" + variableItem.name() + ")")
                        )
                ));
            }

            return readVar;
        } else {
            throw new RuntimeException("Allocations must be variables");
        }
    }

    @Override
    public VarItem<?> readStaticField(String clazz, String field) {
        var readVar = VarPattern.temporary("read");
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "GetDictValue",
                Args.byVarItems(
                        readVar,
                        new VariableItem("statics", "unsaved"),
                        LiteralItem.string(clazz + "." + field)
                )
        ));
        return readVar;
    }

    @Override
    public void invokeVirtual(
            VariableItem callerItem,
            CompilationGraph.MethodOutline methodOutline,
            List<VarItem<?>> parameters,
            boolean process
    ) {
        var classValue = (VariableItem) this.readField(callerItem, LiteralItem.string("class"));
        // TODO: enable classVariable2 when nested %entry is fixed
        var classVariable1 = VarPattern.classInfo("%var(" + classValue.name() + ")").name();
        var classVariable2 = VarPattern.classInfo("%entry(%var(" + callerItem.name() + "),class)").name();
        var methodEntry = VarPattern.methodInfo(methodOutline);
        if (process) {
            this.transformer.appendCodeBlock(ActionBlock.startProcess(
                            "%entry(" + classVariable1 + "," + methodEntry + ")",
                            parameters
                    ).storeTagInSlot(25, "Local Variables", "Don't copy")
                    .storeTagInSlot(26, "Target Mode", "With no targets"));
        } else {
            this.transformer.appendCodeBlock(ActionBlock.callFunction(
                    "%entry(" + classVariable1 + "," + methodEntry + ")",
                    parameters
            ));
        }

    }

    @Override
    public void reference(VarItem<?> allocation) {

    }

    @Override
    public void dereference(VarItem<?> allocation) {

    }

    @Override
    public void cleanup(int locals) {

    }

    @Override
    public List<CodeLine> codeLineContributions(Pipeline pipeline) {
        return List.of();
    }


}
