package dev.akarah.jvm2df.tree.df.strategy.global;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;

import java.util.List;

public class DictHeapStrategy implements GlobalMemoryStrategy {
    CodeLineBuilder transformer;
    LocalMemoryStrategy localMemoryStrategy;

    @Override
    public void setup(CodeLineBuilder transformer, LocalMemoryStrategy locals) {
        this.transformer = transformer;
        this.localMemoryStrategy = locals;
    }

    @Override
    public VarItem<?> allocate() {
        var allocationNameVar = new VariableItem("tmp.allocation", "line");
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "=",
                Args.byVarItems(
                        allocationNameVar,
                        LiteralItem.string("heap.%random(-100000000,100000000)")
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
                                fieldLiteral,
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
        var readVar = new VariableItem("tmp.read." + new Object().hashCode(), "line");
        if (allocation instanceof VariableItem allocationVar) {
            this.transformer.appendCodeBlock(ActionBlock.setVar(
                    "GetDictValue",
                    Args.byVarItems(
                            readVar,
                            new VariableItem("%var(" + allocationVar.name() + ")", "unsaved"),
                            field
                    )
            ));
            return readVar;
        } else {
            throw new RuntimeException("Allocations must be variables");
        }
    }

    @Override
    public VarItem<?> readStaticField(String clazz, String field) {
        var readVar = new VariableItem("tmp.read." + new Object().hashCode(), "line");
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
    public void invokeVirtual(VariableItem callerItem, CompilationGraph.MethodOutline methodOutline, List<VarItem<?>> parameters) {
        this.transformer.appendCodeBlock(ActionBlock.callFunction(
                "%var(class.%entry(%var(" + callerItem.name() + "),class).method." + methodOutline + ")",
                parameters
        ));
    }

    @Override
    public void reference(VarItem<?> allocation) {

    }

    @Override
    public void dereference(VarItem<?> allocation) {

    }
}
