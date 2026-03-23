package dev.akarah.jvm2df.tree.df;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.df.strategy.global.GlobalMemoryStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;

import java.lang.classfile.ClassModel;
import java.util.ArrayList;
import java.util.List;

public class CodeLineBuilder {
    LocalMemoryStrategy locals;
    GlobalMemoryStrategy globals;
    List<CodeBlock<?>> codeLineStack;
    List<CodeLine> confirmedCodeLines;
    CompilationGraph graph;

    public CodeLineBuilder(
            LocalMemoryStrategy locals,
            GlobalMemoryStrategy globals,
            CompilationGraph graph
    ) {
        this.locals = locals;
        this.globals = globals;
        this.graph = graph;
    }

    public LocalMemoryStrategy locals() {
        return this.locals;
    }

    public GlobalMemoryStrategy globals() {
        return this.globals;
    }

    public CompilationGraph graph() {
        return this.graph;
    }

    public void appendCodeBlock(CodeBlock<?> codeBlock) {
        this.codeLineStack.add(codeBlock);
    }

    public VariableItem createListQuickly(List<? extends VarItem<?>> elements) {
        var variable = VarPattern.temporary("created_list");
        var currentSet = new ArrayList<VarItem<?>>();
        currentSet.add(variable);
        int codeblocks = 0;
        for (var value : elements) {
            currentSet.add(value);
            if (currentSet.size() >= 27) {
                if (codeblocks == 0) {
                    this.appendCodeBlock(ActionBlock.setVar(
                            "CreateList",
                            Args.byVarItems(currentSet)
                    ));
                    currentSet.clear();
                    currentSet.add(variable);
                } else {
                    this.appendCodeBlock(ActionBlock.setVar(
                            "AppendValue",
                            Args.byVarItems(currentSet)
                    ));
                    currentSet.clear();
                    currentSet.add(variable);
                }
                codeblocks++;
            }
        }
        if (currentSet.size() > 1) {
            this.appendCodeBlock(ActionBlock.setVar(
                    "AppendValue",
                    Args.byVarItems(currentSet)
            ));
        }
        return variable;
    }

    public void init(ClassModel classModel) {
        this.codeLineStack = new ArrayList<>(new ArrayList<>());
        this.confirmedCodeLines = new ArrayList<>();
        this.codeLineStack = new ArrayList<>();
        this.locals.setup(this, this.globals);
        this.globals.setup(this, this.locals);
    }

    public CodeLine built() {
        var copy = new ArrayList<>(this.codeLineStack);
        this.codeLineStack.clear();
        return new CodeLine(copy);
    }


}
