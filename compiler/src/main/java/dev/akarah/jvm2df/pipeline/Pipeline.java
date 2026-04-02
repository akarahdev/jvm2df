package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.cfg.BytecodeTranslator;
import dev.akarah.jvm2df.tree.cfr.ControlFlowTransformer;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;
import dev.akarah.jvm2df.tree.df.strategy.global.GlobalMemoryStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;

import java.lang.classfile.ClassModel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Pipeline {
    private Path jarPath;
    private final List<PipelineComponent> components = new ArrayList<>();

    private List<ClassModel> classes;
    private CompilationGraph graph;

    private BytecodeTranslator bytecodeTranslator;
    private ControlFlowTransformer flowTransformer;
    private CodeLineBuilder codeLineBuilder;
    private LocalMemoryStrategy localMemoryStrategy;
    private GlobalMemoryStrategy globalMemoryStrategy;

    public Pipeline setJarPath(Path jarPath) {
        this.jarPath = jarPath;
        return this;
    }

    public BytecodeTranslator bytecodeTranslator() {
        return this.bytecodeTranslator;
    }

    public CodeLineBuilder codeLineBuilder() {
        return this.codeLineBuilder;
    }

    public CompilationGraph graph() {
        return this.graph;
    }

    public Pipeline setFlowTransformer(ControlFlowTransformer transformer) {
        this.flowTransformer = transformer;
        return this;
    }

    public ControlFlowTransformer flowTransformer() {
        return this.flowTransformer;
    }

    public Pipeline setLocalMemoryStrategy(LocalMemoryStrategy strategy) {
        this.localMemoryStrategy = strategy;
        return this;
    }

    public LocalMemoryStrategy locals() {
        return this.localMemoryStrategy;
    }

    public Pipeline setGlobalMemoryStrategy(GlobalMemoryStrategy strategy) {
        this.globalMemoryStrategy = strategy;
        return this;
    }

    public GlobalMemoryStrategy globals() {
        return this.globalMemoryStrategy;
    }

    public List<ClassModel> classes() {
        return this.classes;
    }

    public Pipeline registerComponent(PipelineComponent component) {
        this.components.add(component);
        return this;
    }

    public List<CodeLine> execute() {

        this.classes = JarToClasses.convert(this.jarPath);

        var codeLines = new ArrayList<CodeLine>();

        this.graph = new CompilationGraph();
        this.classes.forEach(graph::register);

        this.bytecodeTranslator = new BytecodeTranslator();
        this.codeLineBuilder = new CodeLineBuilder(this.localMemoryStrategy, this.globalMemoryStrategy, graph);

        for (var component : this.components) {
            codeLines.addAll(component.generate(this));
        }

        codeLines = new ArrayList<>(
                codeLines.stream()
                        .filter(x -> !x.codeBlocks().isEmpty())
                        .toList()
        );

        return codeLines;
    }
}
