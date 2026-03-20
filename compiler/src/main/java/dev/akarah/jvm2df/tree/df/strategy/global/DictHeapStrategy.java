package dev.akarah.jvm2df.tree.df.strategy.global;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.Bracket;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.*;
import dev.akarah.jvm2df.pipeline.Pipeline;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;

import java.util.ArrayList;
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
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "SetDictValue",
                Args.byVarItems(
                        VarPattern.gcAllocations(),
                        allocationNameVar,
                        LiteralItem.number("1")
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
    public void invokeVirtual(VariableItem callerItem, CompilationGraph.MethodOutline methodOutline, List<VarItem<?>> parameters) {
        var classValue = (VariableItem) this.readField(callerItem, LiteralItem.string("class"));
        // TODO: enable classVariable2 when nested %entry is fixed
        var classVariable1 = VarPattern.classInfo("%var(" + classValue.name() + ")").name();
        var classVariable2 = VarPattern.classInfo("%entry(%var(" + callerItem.name() + "),class)").name();
        var methodEntry = VarPattern.methodInfo(methodOutline);
        this.transformer.appendCodeBlock(ActionBlock.callFunction(
                "%entry(" + classVariable1 + "," + methodEntry + ")",
                parameters
        ));
    }

    @Override
    public void reference(VarItem<?> allocation) {
        this.transformer.appendCodeBlock(ActionBlock.callFunction(
                "dhs::reference",
                List.of(allocation)
        ));
    }

    @Override
    public void dereference(VarItem<?> allocation) {
        this.transformer.appendCodeBlock(ActionBlock.callFunction(
                "dhs::dereference",
                List.of(allocation)
        ));
    }

    @Override
    public void cleanup(int locals) {
        this.transformer.appendCodeBlock(ActionBlock.callFunction(
                "dhs::dereference",
                List.of(this.localMemoryStrategy.referenceLocal(locals))
        ));
    }

    @Override
    public List<CodeLine> codeLineContributions(Pipeline pipeline) {
        var totals = new ArrayList<CodeLine>();
        totals.add(allocationFunc(pipeline));
        totals.add(deallocationFunc(pipeline));
        return totals;
    }

    private CodeLine allocationFunc(Pipeline pipeline) {
        this.transformer = pipeline.codeLineBuilder();
        this.transformer.init(null);

        this.transformer.appendCodeBlock(
                ActionBlock.function(
                        "dhs::reference",
                        List.of(new ParameterItem("ref", "any", false, false))
                )
        );

        var allocation = new VariableItem("ref", "line");

        this.transformer.appendCodeBlock(ActionBlock.ifVar(
                "StartsWith",
                Args.byVarItems(allocation, LiteralItem.string("heap::"))
        ));
        this.transformer.appendCodeBlock(Bracket.openNormal());
        var out = VarPattern.temporary("root_count");
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "GetDictValue",
                Args.byVarItems(out, VarPattern.gcRoots(), allocation)
        ));
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "+=",
                Args.byVarItems(out, LiteralItem.number("1"))
        ));
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "SetDictValue",
                Args.byVarItems(VarPattern.gcRoots(), allocation, out)
        ));
        this.transformer.appendCodeBlock(Bracket.closeNormal());
        return this.transformer.built();
    }

    private CodeLine deallocationFunc(Pipeline pipeline) {
        this.transformer = pipeline.codeLineBuilder();
        this.transformer.init(null);

        this.transformer.appendCodeBlock(
                ActionBlock.function(
                        "dhs::dereference",
                        List.of(new ParameterItem("ref", "any", false, false))
                )
        );

        var allocation = new VariableItem("ref", "line");

        this.transformer.appendCodeBlock(ActionBlock.ifVar(
                "StartsWith",
                Args.byVarItems(allocation, LiteralItem.string("heap::"))
        ).storeTagInSlot(26, "Ignore Case", "False"));
        this.transformer.appendCodeBlock(Bracket.openNormal());

        var out = VarPattern.temporary("root_count");
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "GetDictValue",
                Args.byVarItems(out, VarPattern.gcRoots(), allocation)
        ));
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "-=",
                Args.byVarItems(out, LiteralItem.number("1"))
        ));

        this.transformer.appendCodeBlock(ActionBlock.ifVar(
                "<=",
                Args.byVarItems(
                        out,
                        LiteralItem.number("0")
                )
        ));
        this.transformer.appendCodeBlock(Bracket.openNormal());
        this.transformer.appendCodeBlock(
                ActionBlock.setVar(
                        "RemoveDictEntry",
                        Args.byVarItems(
                                VarPattern.gcRoots(),
                                allocation
                        )
                )
        );
        this.transformer.appendCodeBlock(Bracket.closeNormal());
        this.transformer.appendCodeBlock(ActionBlock.else_());
        this.transformer.appendCodeBlock(Bracket.openNormal());
        this.transformer.appendCodeBlock(ActionBlock.setVar(
                "SetDictValue",
                Args.byVarItems(VarPattern.gcRoots(), allocation, out)
        ));
        this.transformer.appendCodeBlock(Bracket.closeNormal());
        this.transformer.appendCodeBlock(Bracket.closeNormal());
        return this.transformer.built();
    }
}
