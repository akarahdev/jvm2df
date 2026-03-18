package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.ParameterItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;

import java.lang.classfile.constantpool.ClassEntry;
import java.util.ArrayList;
import java.util.List;

public class GenerateFieldSetup implements PipelineComponent {
    @Override
    public List<CodeLine> generate(Pipeline pipeline) {
        var lines = new ArrayList<CodeLine>();

        var parameter = new ParameterItem("local.0", "any", false, false);
        var variable = new VariableItem("local.0", "line");

        pipeline.classes().forEach(classElements -> {
            pipeline.codeLineBuilder().init(classElements);

            pipeline.codeLineBuilder().appendCodeBlock(
                    ActionBlock.function(
                            functionNameForSetup(classElements.thisClass()),
                            List.of(parameter)
                    )
            );

            for (var outline : pipeline.graph().allSuperMethodsFor(pipeline.graph().classByEntry(classElements.thisClass()))) {
                var methodModel = pipeline.graph().lookupMethodInSuper(
                        classElements.thisClass(),
                        outline.name(),
                        outline.typeDesc()
                );

                pipeline.globals().setField(
                        variable,
                        LiteralItem.string("method." + outline.name() + outline.typeDesc().descriptorString()),
                        LiteralItem.string(pipeline.graph().generateFunctionCallName(
                                methodModel.parent().orElseThrow().thisClass(),
                                outline
                        ))
                );
            }


            lines.add(pipeline.codeLineBuilder().built());
        });
        return lines;
    }

    public static String functionNameForSetup(ClassEntry classEntry) {
        return classEntry.asInternalName() + "#<fieldsetup>()V";
    }
}
