package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.Bracket;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.ParameterItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.df.VarPattern;

import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateFieldSetup implements PipelineComponent {
    private static final String ARRAY_CLONE_FUNCTION = "globals::arrayclone";

    @Override
    public List<CodeLine> generate(Pipeline pipeline) {
        var lines = new ArrayList<CodeLine>();
        var arrayDescriptors = collectArrayDescriptors(pipeline);

        lines.add(generateArrayCloneFunction(pipeline));
        setupNormalClasses(pipeline, lines, arrayDescriptors);
        setupArrays(pipeline, lines, arrayDescriptors);

        pipeline.codeLineBuilder().init(null);
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.gameEvent("PlotStartup"));

//        var hashVar = new VariableItem("globals::comptimehash::" + new Object().hashCode(), "saved");
//        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.ifVar(
//                "!=",
//                Args.byVarItems(hashVar, LiteralItem.number("0"))
//        ));
//        pipeline.codeLineBuilder().appendCodeBlock(Bracket.openNormal());
//        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.control(
//                "End",
//                Args.byVarItems()
//        ));
//        pipeline.codeLineBuilder().appendCodeBlock(Bracket.closeNormal());
//        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
//                "=",
//                Args.byVarItems(hashVar, LiteralItem.number("1"))
//        ));

        var funcsToCall = new ArrayList<>(
                pipeline.classes().stream()
                        .map(x -> x.thisClass().asSymbol().descriptorString() + "#<fieldsetup>()V")
                        .map(LiteralItem::string)
                        .toList()
        );
        funcsToCall.addAll(
                arrayDescriptors.stream()
                        .map(x -> x + "#<fieldsetup>()V")
                        .sorted()
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

    private void setupNormalClasses(Pipeline pipeline, List<CodeLine> lines, Set<String> arrayDescriptors) {
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
                System.out.println(classElements.thisClass());
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
            methodKeys.add(LiteralItem.string(VarPattern.classesExtendingThisClass()));
            var extendingDescriptors = pipeline.graph().allClassesExtending(classElements.thisClass())
                    .stream().map(x -> x.asSymbol().descriptorString())
                    .collect(Collectors.joining());
            if (classElements.thisClass().asSymbol().descriptorString().equals("Ljava/lang/Object;")) {
                extendingDescriptors += String.join("", arrayDescriptors);
            }
            methodValues.add(LiteralItem.string(
                    extendingDescriptors
            ));

            var av = pipeline.codeLineBuilder().createListQuickly(methodKeys);
            var bv = pipeline.codeLineBuilder().createListQuickly(methodValues);
            pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                    "CreateDict",
                    Args.byVarItems(classVariable, av, bv)
            ));
            lines.add(pipeline.codeLineBuilder().built());
        });
    }

    private void setupArrays(Pipeline pipeline, List<CodeLine> lines, Set<String> arrayDescriptors) {
        if (arrayDescriptors.isEmpty()) {
            return;
        }
        var cloneVirtualOutline = new CompilationGraph.MethodOutline(
                "clone",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Object"))
        );

        for (var descriptor : arrayDescriptors) {
            var methodKeys = new ArrayList<LiteralItem>();
            var methodValues = new ArrayList<LiteralItem>();

            pipeline.codeLineBuilder().init(null);

            pipeline.codeLineBuilder().appendCodeBlock(
                    ActionBlock.function(
                            descriptor + "#<fieldsetup>()V",
                            List.of()
                    )
            );

            var classVariable = VarPattern.classInfo(descriptor);

            pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                    "CreateDict",
                    Args.byVarItems(classVariable)
            ));

            methodKeys.add(LiteralItem.string(VarPattern.methodInfo(cloneVirtualOutline)));
            methodValues.add(LiteralItem.string(ARRAY_CLONE_FUNCTION));
            methodKeys.add(LiteralItem.string(VarPattern.classesExtendingThisClass()));
            methodValues.add(LiteralItem.string(descriptor));

            var av = pipeline.codeLineBuilder().createListQuickly(methodKeys);
            var bv = pipeline.codeLineBuilder().createListQuickly(methodValues);
            pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                    "CreateDict",
                    Args.byVarItems(classVariable, av, bv)
            ));
            lines.add(pipeline.codeLineBuilder().built());
        }
    }

    private Set<String> collectArrayDescriptors(Pipeline pipeline) {
        var arrayDescriptors = new LinkedHashSet<String>();

        pipeline.classes().forEach(classElements -> classElements.methods().forEach(methodElements -> {
            addArrayDescriptor(arrayDescriptors, methodElements.methodTypeSymbol().returnType().descriptorString());
            methodElements.methodTypeSymbol().parameterList().forEach(
                    parameter -> addArrayDescriptor(arrayDescriptors, parameter.descriptorString())
            );

            methodElements.code().ifPresent(codeModel -> codeModel.elementList().forEach(element -> {
                switch (element) {
                    case NewPrimitiveArrayInstruction instruction ->
                            addArrayDescriptor(arrayDescriptors, "[" + primitiveTypeDescriptor(instruction.typeKind()));
                    case NewReferenceArrayInstruction instruction ->
                            addArrayDescriptor(arrayDescriptors, "[" + instruction.componentType().asSymbol().descriptorString());
                    case NewMultiArrayInstruction instruction ->
                            addArrayDescriptor(arrayDescriptors, instruction.arrayType().asSymbol().descriptorString());
                    case InvokeInstruction instruction ->
                            addArrayDescriptor(arrayDescriptors, instruction.method().owner().asSymbol().descriptorString());
                    case TypeCheckInstruction instruction ->
                            addArrayDescriptor(arrayDescriptors, instruction.type().asSymbol().descriptorString());
                    default -> {
                    }
                }
            }));
        }));

        return arrayDescriptors;
    }

    private void addArrayDescriptor(Set<String> descriptors, String descriptor) {
        if (descriptor.startsWith("[")) {
            descriptors.add(descriptor);
        }
    }

    private String primitiveTypeDescriptor(TypeKind kind) {
        return switch (kind) {
            case BOOLEAN -> "Z";
            case BYTE -> "B";
            case CHAR -> "C";
            case SHORT -> "S";
            case INT -> "I";
            case LONG -> "J";
            case FLOAT -> "F";
            case DOUBLE -> "D";
            default -> throw new IllegalArgumentException("Unsupported primitive array element kind: " + kind);
        };
    }

    private CodeLine generateArrayCloneFunction(Pipeline pipeline) {
        pipeline.codeLineBuilder().init(null);
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.function(
                ARRAY_CLONE_FUNCTION,
                List.of(
                        new ParameterItem("return", "var", false, false),
                        new ParameterItem("array", "any", false, false)
                )
        ));

        var arrayVar = new VariableItem("array", "line");
        var keyVar = new VariableItem("key", "line");
        var valueVar = new VariableItem("value", "line");
        var out = pipeline.codeLineBuilder().globals().allocate();

        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.repeat(
                "ForEachEntry",
                Args.byVarItems(keyVar, valueVar, new VariableItem("%var(" + arrayVar.name() + ")", "unsaved"))
        ));
        pipeline.codeLineBuilder().appendCodeBlock(Bracket.openRepeat());
        pipeline.codeLineBuilder().globals().setField(out, keyVar, valueVar);
        pipeline.codeLineBuilder().appendCodeBlock(Bracket.closeRepeat());

        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.setVar(
                "=",
                Args.byVarItems(new VariableItem("return", "line"), out)
        ));
        pipeline.codeLineBuilder().appendCodeBlock(ActionBlock.control("Return", Args.byVarItems()));
        return pipeline.codeLineBuilder().built();
    }
}
