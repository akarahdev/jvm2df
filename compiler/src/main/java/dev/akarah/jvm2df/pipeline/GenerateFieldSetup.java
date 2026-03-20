package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.tree.df.VarPattern;

import java.util.ArrayList;
import java.util.List;

public class GenerateFieldSetup implements PipelineComponent {
    @Override
    public List<CodeLine> generate(Pipeline pipeline) {
        var lines = new ArrayList<CodeLine>();

        pipeline.classes().forEach(classElements -> {
            pipeline.codeLineBuilder().init(classElements);

            pipeline.codeLineBuilder().appendCodeBlock(
                    ActionBlock.function(
                            classElements.thisClass().asInternalName() + "#<fieldsetup>()V",
                            List.of()
                    )
            );

            var classVariable = VarPattern.classInfo(classElements.thisClass());

            pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                    "CreateDict",
                    Args.byVarItems(classVariable)
            ));

            for (var outline : pipeline.graph().allSuperMethodsFor(pipeline.graph().classByEntry(classElements.thisClass()))) {
                var methodModel = pipeline.graph().lookupMethodInSuper(
                        classElements.thisClass(),
                        outline.name(),
                        outline.typeDesc()
                );

                pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                        "SetDictValue",
                        Args.byVarItems(
                                classVariable,
                                LiteralItem.string(VarPattern.methodInfo(outline)),
                                LiteralItem.string(pipeline.graph().generateFunctionCallName(
                                        methodModel.parent().orElseThrow().thisClass(),
                                        outline
                                ))
                        )
                ));
            }


            lines.add(pipeline.codeLineBuilder().built());
        });

        pipeline.codeLineBuilder().init(null);
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.gameEvent("PlotStartup"));
        pipeline.classes().forEach(classElements -> {
            pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.callFunction(
                    classElements.thisClass().asInternalName() + "#<fieldsetup>()V",
                    List.of()
            ));
        });
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                "CreateDict",
                Args.byVarItems(VarPattern.gcRoots())
        ));
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                "CreateDict",
                Args.byVarItems(VarPattern.gcAllocations())
        ));
        lines.add(pipeline.codeLineBuilder().built());
        return lines;
    }
}
