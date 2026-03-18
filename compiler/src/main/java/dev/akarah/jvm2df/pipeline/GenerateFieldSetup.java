package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;

import java.lang.classfile.constantpool.ClassEntry;
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
                            functionNameForSetup(classElements.thisClass()),
                            List.of()
                    )
            );

            for (var outline : pipeline.graph().allSuperMethodsFor(pipeline.graph().classByEntry(classElements.thisClass()))) {
                var methodModel = pipeline.graph().lookupMethodInSuper(
                        classElements.thisClass(),
                        outline.name(),
                        outline.typeDesc()
                );

                pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(
                                new VariableItem(
                                        "class." + classElements.thisClass().asInternalName()
                                                + ".method." + outline.name() + outline.typeDesc().descriptorString(),
                                        "unsaved"),
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
        lines.add(pipeline.codeLineBuilder().built());
        return lines;
    }

    public static String functionNameForSetup(ClassEntry classEntry) {
        return classEntry.asInternalName() + "#<fieldsetup>()V";
    }
}
