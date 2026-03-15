package dev.akarah.jvm2df.frontend;

import dev.akarah.jvm2df.bytecode.JarToClasses;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.cfg.BasicBlock;
import dev.akarah.jvm2df.tree.cfg.BytecodeTranslator;
import dev.akarah.jvm2df.tree.cfr.ControlFlowTransformer;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.df.strategy.global.GlobalMemoryStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;
import dev.akarah.jvm2df.tree.instructions.WithContext;
import dev.akarah.jvm2df.util.Beep;

import javax.sound.sampled.LineUnavailableException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Pipeline {
    Path jarPath;
    BytecodeTranslator bytecodeTranslator = new BytecodeTranslator();
    ControlFlowTransformer flowTransformer;
    CodeBlockTransformer codeBlockTransformer = new CodeBlockTransformer();
    LocalMemoryStrategy localMemoryStrategy;
    GlobalMemoryStrategy globalMemoryStrategy;

    public Pipeline setJarPath(Path jarPath) {
        this.jarPath = jarPath;
        return this;
    }

    public Pipeline setFlowTransformer(ControlFlowTransformer transformer) {
        this.flowTransformer = transformer;
        return this;
    }

    public Pipeline setLocalMemoryStrategy(LocalMemoryStrategy strategy) {
        this.localMemoryStrategy = strategy;
        return this;
    }

    public Pipeline setGlobalMemoryStrategy(GlobalMemoryStrategy strategy) {
        this.globalMemoryStrategy = strategy;
        return this;
    }

    public List<CodeLine> execute() {
        var classes = JarToClasses.convert(this.jarPath);

        final var codeLines = new ArrayList<CodeLine>();

        var graph = new CompilationGraph();
        classes.forEach(graph::register);
        classes.forEach(classElements -> {
            classElements.methods().forEach(methodElements -> {
                methodElements.code().ifPresent(codeModel -> {
                    try {
                        System.out.println(classElements.thisClass().asInternalName() + "#" + methodElements.methodName() + methodElements.methodTypeSymbol().descriptorString());
                        System.out.println(codeModel.toDebugString());
                        var basicBlocks = this.bytecodeTranslator.split(codeModel, graph);
                        System.out.println(basicBlocks);
                        var flowBlock = this.flowTransformer.convert(basicBlocks);
                        System.out.println(flowBlock);
                        var newLines = this.codeBlockTransformer.transform(
                                flowBlock,
                                methodElements,
                                this.localMemoryStrategy,
                                this.globalMemoryStrategy,
                                graph
                        );
                        codeLines.addAll(newLines);
                    } catch (Exception e) {
                        try {
                            Beep.tone(330, 100, 0.5);
                        } catch (LineUnavailableException _) {
                            // ignore it, sound isn't necessary
                        }
                        throw new RuntimeException(e);
                    }
                });
            });
        });

        return codeLines;
    }
}
