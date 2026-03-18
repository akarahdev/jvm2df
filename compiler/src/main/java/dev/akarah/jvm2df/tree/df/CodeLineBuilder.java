package dev.akarah.jvm2df.tree.df;

import dev.akarah.jvm2df.codetemplate.blocks.CodeBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
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

    public LocalMemoryStrategy localMemoryStrategy() {
        return this.locals;
    }

    public GlobalMemoryStrategy globalMemoryStrategy() {
        return this.globals;
    }

    public void appendCodeBlock(CodeBlock<?> codeBlock) {
        this.codeLineStack.add(codeBlock);
    }

    public void init(ClassModel classModel) {
        this.codeLineStack = new ArrayList<>(new ArrayList<>());
        this.confirmedCodeLines = new ArrayList<>();
        this.codeLineStack = new ArrayList<>();
        this.locals.setup(this);
        this.globals.setup(this, locals);
    }

    public CodeLine built() {
        var copy = new ArrayList<>(this.codeLineStack);
        this.codeLineStack.clear();
        return new CodeLine(copy);
    }


}
