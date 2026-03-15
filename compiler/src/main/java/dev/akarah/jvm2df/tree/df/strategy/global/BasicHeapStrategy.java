package dev.akarah.jvm2df.tree.df.strategy.global;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;

public class BasicHeapStrategy implements GlobalMemoryStrategy {
    CodeBlockTransformer transformer;
    LocalMemoryStrategy localMemoryStrategy;

    @Override
    public void setup(CodeBlockTransformer transformer, LocalMemoryStrategy locals) {
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
        return allocationNameVar;
    }

    @Override
    public void setField(VarItem<?> allocation, VarItem<?> field, VarItem<?> value) {
        if(allocation instanceof VariableItem allocationVar) {
            if(field instanceof LiteralItem fieldLiteral) {
                this.transformer.appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(
                                new VariableItem("%var(" + allocationVar.name() + ")." + fieldLiteral.value(), "unsaved"),
                                value
                        )
                ));
            } else if(field instanceof VariableItem fieldVar) {
                this.transformer.appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(
                                new VariableItem("%var(" + allocationVar.name() + ").%var(" + fieldVar.name() + ")", "unsaved"),
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
                "=",
                Args.byVarItems(
                        new VariableItem("static." + clazz + "." + field, "unsaved"),
                        value
                )
        ));
    }

    @Override
    public VarItem<?> readField(VarItem<?> allocation, VarItem<?> field) {
        if(allocation instanceof VariableItem allocationVar) {
            if(field instanceof LiteralItem fieldLiteral) {
                return new VariableItem("%var(" + allocationVar.name() + ")." + fieldLiteral.value(), "unsaved");
            } else if(field instanceof VariableItem fieldVar) {
                return new VariableItem("%var(" + allocationVar.name() + ").%var(" + fieldVar.name() + ")", "unsaved");
            } else {
                throw new RuntimeException("Fields must be literals or variables");
            }
        } else {
            throw new RuntimeException("Allocations must be variables");
        }
    }

    @Override
    public VarItem<?> readStaticField(String clazz, String field) {
        return new VariableItem("static." + clazz + "." + field, "unsaved");
    }

    @Override
    public void reference(VarItem<?> allocation) {

    }

    @Override
    public void dereference(VarItem<?> allocation) {

    }
}
