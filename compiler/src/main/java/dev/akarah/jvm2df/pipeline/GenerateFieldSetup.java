package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.Bracket;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.tree.df.VarPattern;

import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;

public class GenerateFieldSetup implements PipelineComponent {
    @Override
    public List<CodeLine> generate(Pipeline pipeline) {
        var lines = new ArrayList<CodeLine>();

        pipeline.classes().forEach(classElements -> {
            var methodKeys = new ArrayList<LiteralItem>();
            var methodValues = new ArrayList<LiteralItem>();

            pipeline.codeLineBuilder().init(classElements);

            pipeline.codeLineBuilder().appendCodeBlock(
                    ActionBlock.function(
                            classElements.thisClass().asSymbol().descriptorString() + "#<fieldsetup>()V",
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
                if (methodModel.flags().has(AccessFlag.STATIC)) {
                    continue;
                }
                if (methodModel.flags().has(AccessFlag.ABSTRACT)) {
                    return;
                }
                if (methodModel.flags().has(AccessFlag.INTERFACE)) {
                    return;
                }
                methodKeys.add(LiteralItem.string(VarPattern.methodInfo(outline)));
                methodValues.add(LiteralItem.string(pipeline.graph().generateFunctionCallName(
                        methodModel.parent().orElseThrow().thisClass(),
                        outline
                )));
            }

            var av = pipeline.codeLineBuilder().createListQuickly(methodKeys);
            var bv = pipeline.codeLineBuilder().createListQuickly(methodValues);
            pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                    "CreateDict",
                    Args.byVarItems(classVariable, av, bv)
            ));
            lines.add(pipeline.codeLineBuilder().built());
        });

        pipeline.codeLineBuilder().init(null);
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.gameEvent("PlotStartup"));

        var funcsToCall = new ArrayList<>(
                pipeline.classes().stream()
                        .map(x -> x.thisClass().asSymbol().descriptorString() + "#<fieldsetup>()V")
                        .map(LiteralItem::string)
                        .toList()
        );
        funcsToCall.addAll(
                pipeline.classes().stream()
                        .map(x -> x.thisClass().asSymbol().descriptorString() + "#<clinit>()V")
                        .map(LiteralItem::string)
                        .toList()
        );
        var tmp = VarPattern.temporary("class");
        var createdList = pipeline.codeLineBuilder().createListQuickly(funcsToCall);
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.repeat(
                "ForEach",
                Args.byVarItems(tmp, createdList)
        ).storeTagInSlot(26, "Allow List Changes", "True"));
        pipeline.codeLineBuilder().appendCodeBlock(Bracket.openRepeat());
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.callFunction(
                "%var(" + tmp.name() + ")",
                List.of()
        ));
        pipeline.codeLineBuilder().appendCodeBlock(Bracket.closeRepeat());
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
