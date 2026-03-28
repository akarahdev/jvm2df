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

public class TracingGCStrategy implements GlobalMemoryStrategy {
    GlobalMemoryStrategy inner;

    public TracingGCStrategy(GlobalMemoryStrategy inner) {
        this.inner = inner;
    }

    @Override
    public CodeLineBuilder transformer() {
        return this.inner.transformer();
    }

    @Override
    public LocalMemoryStrategy locals() {
        return this.inner.locals();
    }

    @Override
    public void setup(CodeLineBuilder transformer, LocalMemoryStrategy locals) {
        this.inner.setup(transformer, locals);
    }

    @Override
    public VarItem<?> allocate() {
        var value = this.inner.allocate();
        this.setField(value, LiteralItem.string("dhs::marked"), LiteralItem.number("0"));
        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "SetDictValue",
                Args.byVarItems(
                        VarPattern.gcAllocations(),
                        value,
                        LiteralItem.number("1")
                )
        ));
        return value;
    }

    @Override
    public void setField(VarItem<?> allocation, VarItem<?> field, VarItem<?> value) {
        this.inner.setField(allocation, field, value);
    }

    @Override
    public void setStaticField(String clazz, String field, VarItem<?> value) {
        this.inner.setStaticField(clazz, field, value);
    }

    @Override
    public VarItem<?> readField(VarItem<?> allocation, VarItem<?> field) {
        return this.inner.readField(allocation, field);
    }

    @Override
    public VarItem<?> readStaticField(String clazz, String field) {
        return this.inner.readStaticField(clazz, field);
    }

    @Override
    public void invokeVirtual(
            VarItem<?> callerItem,
            CompilationGraph.MethodOutline methodOutline,
            List<VarItem<?>> parameters,
            boolean process
    ) {
        this.inner.invokeVirtual(callerItem, methodOutline, parameters, process);
    }

    @Override
    public void reference(VarItem<?> allocation) {
        this.inner.transformer().appendCodeBlock(ActionBlock.callFunction(
                "dhs::reference",
                List.of(allocation)
        ));
    }

    @Override
    public void dereference(VarItem<?> allocation) {
        this.inner.transformer().appendCodeBlock(ActionBlock.callFunction(
                "dhs::dereference",
                List.of(allocation)
        ));
    }

    @Override
    public void cleanup(int locals) {
        this.inner.transformer().appendCodeBlock(ActionBlock.callFunction(
                "dhs::dereference",
                List.of(this.locals().referenceLocal(locals))
        ));
    }

    @Override
    public List<CodeLine> codeLineContributions(Pipeline pipeline) {
        var totals = new ArrayList<CodeLine>(this.inner.codeLineContributions(pipeline));
        totals.add(allocationFunc(pipeline));
        totals.add(deallocationFunc(pipeline));
        totals.add(gcFunc(pipeline));
        return totals;
    }

    private CodeLine allocationFunc(Pipeline pipeline) {
        this.transformer().init(null);

        this.transformer().appendCodeBlock(
                ActionBlock.function(
                        "dhs::reference",
                        List.of(new ParameterItem("ref", "any", false, false))
                )
        );

        var allocation = new VariableItem("ref", "line");

        this.transformer().appendCodeBlock(ActionBlock.ifVar(
                "StartsWith",
                Args.byVarItems(allocation, LiteralItem.string("heap::"))
        ).storeTagInSlot(26, "Ignore Case", "False"));
        this.transformer().appendCodeBlock(Bracket.openNormal());
        var out = VarPattern.temporary("root_count");
        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "GetDictValue",
                Args.byVarItems(out, VarPattern.gcRoots(), allocation)
        ));
        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "+=",
                Args.byVarItems(out, LiteralItem.number("1"))
        ));
        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "SetDictValue",
                Args.byVarItems(VarPattern.gcRoots(), allocation, out)
        ));
        this.transformer().appendCodeBlock(Bracket.closeNormal());
        return this.transformer().built();
    }

    private CodeLine deallocationFunc(Pipeline pipeline) {
        this.inner.transformer().init(null);

        this.transformer().appendCodeBlock(
                ActionBlock.function(
                        "dhs::dereference",
                        List.of(new ParameterItem("ref", "any", false, false))
                )
        );

        var allocation = new VariableItem("ref", "line");

        this.transformer().appendCodeBlock(ActionBlock.ifVar(
                "StartsWith",
                Args.byVarItems(allocation, LiteralItem.string("heap::"))
        ).storeTagInSlot(26, "Ignore Case", "False"));
        this.transformer().appendCodeBlock(Bracket.openNormal());

        var out = VarPattern.temporary("root_count");
        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "GetDictValue",
                Args.byVarItems(out, VarPattern.gcRoots(), allocation)
        ));
        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "-=",
                Args.byVarItems(out, LiteralItem.number("1"))
        ));

        this.transformer().appendCodeBlock(ActionBlock.ifVar(
                "<=",
                Args.byVarItems(
                        out,
                        LiteralItem.number("0")
                )
        ));
        this.transformer().appendCodeBlock(Bracket.openNormal());
        {
            this.transformer().appendCodeBlock(
                    ActionBlock.setVar(
                            "RemoveDictEntry",
                            Args.byVarItems(
                                    VarPattern.gcRoots(),
                                    allocation
                            )
                    )
            );
        }
        this.transformer().appendCodeBlock(Bracket.closeNormal());
        this.transformer().appendCodeBlock(ActionBlock.else_());
        this.transformer().appendCodeBlock(Bracket.openNormal());
        {
            this.transformer().appendCodeBlock(ActionBlock.setVar(
                    "SetDictValue",
                    Args.byVarItems(VarPattern.gcRoots(), allocation, out)
            ));
        }
        this.transformer().appendCodeBlock(Bracket.closeNormal());
        this.transformer().appendCodeBlock(Bracket.closeNormal());
        return this.transformer().built();
    }

    private CodeLine gcFunc(Pipeline pipeline) {
        this.inner.transformer().init(null);

        this.transformer().appendCodeBlock(
                ActionBlock.function(
                        VarPattern.gcFunc(),
                        List.of()
                )
        );

        var workList = new VariableItem("worklist", "line");

        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "CreateList",
                Args.byVarItems(workList)
        ));

        // iterate thru gc roots, collect all of them and mark them as present
        var unused = new VariableItem("_", "line");
        var root = new VariableItem("root", "line");

        this.transformer().appendCodeBlock(ActionBlock.repeat(
                "ForEachEntry",
                Args.byVarItems(root, unused, VarPattern.gcRoots())
        ));
        this.transformer().appendCodeBlock(Bracket.openRepeat());
        {
            var mark = this.inner.readField(root, LiteralItem.string("dhs::marked"));
            this.transformer().appendCodeBlock(ActionBlock.ifVar(
                    "=",
                    Args.byVarItems(mark, LiteralItem.number("0"))
            ));

            this.transformer().appendCodeBlock(Bracket.openNormal());
            {
                this.inner.setField(root, LiteralItem.string("dhs::marked"), LiteralItem.number("1"));
                this.transformer().appendCodeBlock(ActionBlock.setVar(
                        "AppendValue",
                        Args.byVarItems(workList, root)
                ));
            }
            this.transformer().appendCodeBlock(Bracket.closeNormal());
        }
        this.transformer().appendCodeBlock(Bracket.closeRepeat());

        // now, loop thru the work list and recursively mark
        // we do an iterative approach here so we don't have to cope with cycles and multiple functions
        this.transformer().appendCodeBlock(ActionBlock.repeat(
                "While",
                "ListSizeEquals",
                Args.byVarItems(workList),
                "NOT"
        ));
        this.transformer().appendCodeBlock(Bracket.openRepeat());
        {
            var obj = new VariableItem("obj", "line");
            this.transformer().appendCodeBlock(ActionBlock.setVar(
                    "PopListValue",
                    Args.byVarItems(obj, workList)
            ));
            var key = new VariableItem("key", "line");
            var value = new VariableItem("value", "line");
            this.transformer().appendCodeBlock(ActionBlock.repeat(
                    "ForEachEntry",
                    Args.byVarItems(key, value, new VariableItem("%var(obj)", "unsaved"))
            ));
            this.transformer().appendCodeBlock(Bracket.openRepeat());
            {
                this.transformer().appendCodeBlock(ActionBlock.ifVar(
                        "StartsWith",
                        Args.byVarItems(value, LiteralItem.string("heap::"))
                ).storeTagInSlot(26, "Ignore Case", "False"));
                this.transformer().appendCodeBlock(Bracket.openNormal());
                {
                    var markValue = this.readField(value, LiteralItem.string("dhs::marked"));
                    this.transformer().appendCodeBlock(ActionBlock.ifVar(
                            "=",
                            Args.byVarItems(markValue, LiteralItem.number("0"))
                    ));
                    this.transformer().appendCodeBlock(Bracket.openNormal());
                    {
                        this.setField(value, LiteralItem.string("dhs::marked"), LiteralItem.number("1"));
                        this.transformer().appendCodeBlock(ActionBlock.setVar(
                                "AppendValue",
                                Args.byVarItems(workList, value)
                        ));
                    }
                    this.transformer().appendCodeBlock(Bracket.closeNormal());
                }
                this.transformer().appendCodeBlock(Bracket.closeNormal());
            }
            this.transformer().appendCodeBlock(Bracket.closeRepeat());
        }
        this.transformer().appendCodeBlock(Bracket.closeRepeat());


        // now that every allocation is marked, we can sweep along them
        // if a thing is marked, unmark it
        // if a thing is not marked, free it :)
        var currentAllocation = new VariableItem("allocation", "line");
        var allocationKeys = new VariableItem("allocationKeys", "line");
        this.transformer().appendCodeBlock(ActionBlock.setVar(
                "GetDictKeys",
                Args.byVarItems(allocationKeys, VarPattern.gcAllocations())
        ));
        this.transformer().appendCodeBlock(ActionBlock.repeat(
                "ForEach",
                Args.byVarItems(currentAllocation, allocationKeys)
        ).storeTagInSlot(26, "Allow List Changes", "True"));
        this.transformer().appendCodeBlock(Bracket.openRepeat());
        {
            var isMarked = this.inner.readField(currentAllocation, LiteralItem.string("dhs::marked"));
            this.transformer().appendCodeBlock(ActionBlock.ifVar(
                    "=",
                    Args.byVarItems(isMarked, LiteralItem.number("0"))
            ));
            this.transformer().appendCodeBlock(Bracket.openNormal());
            {
                this.transformer().appendCodeBlock(ActionBlock.setVar(
                                "PurgeVars",
                                Args.byVarItems(currentAllocation)
                        ).storeTagInSlot(25, "Match Requirement", "Entire name")
                        .storeTagInSlot(26, "Ignore Case", "False"));
                this.transformer().appendCodeBlock(ActionBlock.setVar(
                        "RemoveDictEntry",
                        Args.byVarItems(VarPattern.gcAllocations(), currentAllocation)
                ));
            }
            this.transformer().appendCodeBlock(Bracket.closeNormal());
            this.transformer().appendCodeBlock(ActionBlock.else_());
            this.transformer().appendCodeBlock(Bracket.openNormal());
            {
                this.setField(currentAllocation, LiteralItem.string("dhs::marked"), LiteralItem.number("0"));
            }
            this.transformer().appendCodeBlock(Bracket.closeNormal());
        }
        this.transformer().appendCodeBlock(Bracket.closeRepeat());

        return this.transformer().built();
    }
}
