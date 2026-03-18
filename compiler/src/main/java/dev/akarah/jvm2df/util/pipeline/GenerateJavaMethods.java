package dev.akarah.jvm2df.util.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.tree.df.FlowToDF;
import dev.akarah.jvm2df.util.Beep;

import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

public class GenerateJavaMethods implements PipelineComponent {
    @Override
    public List<CodeLine> generate(Pipeline pipeline) {
        final var codeLines = new ArrayList<CodeLine>();

        pipeline.classes().forEach(classElements -> {
            classElements.methods().forEach(methodElements -> {
                methodElements.code().ifPresent(codeModel -> {
                    try {
                        System.out.println(classElements.thisClass().asInternalName() + "#" + methodElements.methodName() + methodElements.methodTypeSymbol().descriptorString());
                        System.out.println(codeModel.toDebugString());
                        var basicBlocks = pipeline.bytecodeTranslator().split(methodElements, codeModel, pipeline.graph());
                        System.out.println(basicBlocks);
                        var flowBlock = pipeline.flowTransformer().convert(basicBlocks);
                        System.out.println(flowBlock);
                        var flow = new FlowToDF(pipeline.codeLineBuilder());
                        var newLines = flow.transformMethod(flowBlock, methodElements);
                        codeLines.add(newLines);
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
