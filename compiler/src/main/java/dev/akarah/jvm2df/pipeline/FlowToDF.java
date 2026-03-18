package dev.akarah.jvm2df.pipeline;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.Bracket;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.df.CodeLineBuilder;
import dev.akarah.jvm2df.tree.df.handler.InvokeHandler;
import dev.akarah.jvm2df.tree.df.strategy.global.GlobalMemoryStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.ComparisonType;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FlowToDF {
    CodeLineBuilder builder;

    public FlowToDF(
            LocalMemoryStrategy locals,
            GlobalMemoryStrategy globals,
            CompilationGraph graph
    ) {
        this.builder = new CodeLineBuilder(
                locals,
                globals,
                graph
        );
    }

    public FlowToDF(CodeLineBuilder builder) {
        this.builder = builder;
    }

    public CodeLineBuilder builder() {
        return this.builder;
    }

    public CodeLine transformMethod(
            FlowBlock block,
            MethodModel methodModel
    ) {
        this.builder.init(methodModel.parent().orElseThrow());

        var params = new ArrayList<VarItem<?>>(this.builder.locals().functionHeadParams(methodModel));

        var parentalName = methodModel.parent()
                .flatMap(ClassModel::superclass)
                .map(ClassEntry::asInternalName)
                .orElse("java/lang/Object");

        switch (parentalName) {
            case "diamondfire/event/PlayerEventHandler" -> {
                if (methodModel.methodName().equalsString("<init>") || methodModel.methodName().equalsString("<clinit>")) {
                    return this.builder.built();
                }
                this.builder.appendCodeBlock(ActionBlock.playerEvent(methodModel.methodName().stringValue()));
            }
            case "diamondfire/event/EntityEventHandler" -> {
                if (methodModel.methodName().equalsString("<init>") || methodModel.methodName().equalsString("<clinit>")) {
                    return this.builder.built();
                }
                this.builder.appendCodeBlock(ActionBlock.entityEvent(methodModel.methodName().stringValue()));
            }
            case "diamondfire/event/GameEventHandler" -> {
                if (methodModel.methodName().equalsString("<init>") || methodModel.methodName().equalsString("<clinit>")) {
                    return this.builder.built();
                }
                this.builder.appendCodeBlock(ActionBlock.gameEvent(methodModel.methodName().stringValue()));
            }
            default -> {
                this.builder.appendCodeBlock(ActionBlock.function(
                        methodModel.parent().orElseThrow().thisClass().asInternalName()
                                + "#"
                                + methodModel.methodName().stringValue()
                                + methodModel.methodTypeSymbol().descriptorString(),
                        params
                ));
            }
        }

        this.convertFlowBlock(block);

        return this.builder.built();
    }

    public void convertFlowBlock(FlowBlock block) {
        for (var tree : block.statements()) {
            this.convertCodeTree(tree);
        }
    }

    public VarItem<?> convertCodeTree(CodeTree codeTree) {
        return switch (codeTree) {
            case CodeTree.Constant constant -> switch (constant.constantDesc()) {
                case Integer i -> LiteralItem.number(i.toString());
                case Long i -> LiteralItem.number(i.toString());
                case Float i -> LiteralItem.number(i.toString());
                case Double i -> LiteralItem.number(i.toString());
                case String string -> LiteralItem.string(string);
                default -> {
                    throw new RuntimeException("I can't handle this right now :(");
                }
            };
            case CodeTree.StoreLocal(int idx, CodeTree value) -> {
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(this.builder.locals().referenceLocal(idx), this.convertCodeTree(value))
                ));
                yield LiteralItem.number("0");
            }
            case CodeTree.LoadLocal(int idx) -> this.builder.locals().referenceLocal(idx);
            case CodeTree.ExecuteFlow(ReconstructedFlow flow) -> this.convertFlowOperation(flow);
            case Terminator.ReturnVoid _ -> {
                this.builder.locals().setResultAndReturn(LiteralItem.number("0"));
                yield LiteralItem.number("0");
            }
            case Terminator.ReturnValue ret -> {
                this.builder.locals().setResultAndReturn(this.convertCodeTree(ret.code()));
                yield LiteralItem.number("0");
            }
            case Terminator.Break _ -> {
                this.builder.appendCodeBlock(ActionBlock.control("StopRepeat", Args.byVarItems()));
                yield LiteralItem.number("0");
            }
            case Terminator.Continue _ -> {
                this.builder.appendCodeBlock(ActionBlock.control("Skip", Args.byVarItems()));
                yield LiteralItem.number("0");
            }
            case CodeTree.Invoke invoke -> this.convertInvoke(invoke);
            case CodeTree.Compare compare -> this.convertCompare(compare);
            case CodeTree.BinOp binOp -> this.convertBinOp(binOp);
            case CodeTree.IncrementLocal inc -> {
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "+=",
                        Args.byVarItems(
                                this.builder.locals().referenceLocal(inc.idx()),
                                this.convertCodeTree(inc.value())
                        )
                ));
                yield this.builder.locals().referenceLocal(inc.idx());
            }
            case CodeTree.ArrayNew arrayNew -> {
                var alloc = this.builder.globals().allocate();
                this.builder.globals().setField(
                        alloc,
                        LiteralItem.string("length"),
                        this.convertCodeTree(arrayNew.size())
                );
                yield alloc;
            }
            case CodeTree.ArrayStore arrayStore -> {
                var array = this.convertCodeTree(arrayStore.list());
                this.builder.globals().setField(
                        array,
                        this.convertCodeTree(arrayStore.index()),
                        this.convertCodeTree(arrayStore.value())
                );
                yield array;
            }
            case CodeTree.ArrayIndex arrayIndex -> this.builder.globals().readField(
                    this.convertCodeTree(arrayIndex.list()),
                    this.convertCodeTree(arrayIndex.index())
            );
            case CodeTree.ArrayLength arrayLength -> this.builder.globals().readField(
                    this.convertCodeTree(arrayLength.list()),
                    LiteralItem.string("length")
            );
            case CodeTree.ObjectNew objectNew -> {
                var alloc = this.builder.globals().allocate();
                this.builder.globals().setField(
                        alloc,
                        LiteralItem.string("class"),
                        LiteralItem.string(objectNew.clazz())
                );
                yield alloc;
            }
            case CodeTree.ObjectSetStatic(String clazz, String field, CodeTree value) -> {
                this.builder.globals().setStaticField(clazz, field, this.convertCodeTree(value));
                yield LiteralItem.number("0");
            }
            case CodeTree.ObjectGetStatic(String clazz, String field) ->
                    this.builder.globals().readStaticField(clazz, field);
            case CodeTree.ObjectSetField objStore -> {
                var array = this.convertCodeTree(objStore.obj());
                this.builder.globals().setField(
                        array,
                        LiteralItem.string(objStore.field()),
                        this.convertCodeTree(objStore.value())
                );
                yield array;
            }
            case CodeTree.ObjectGetField objIndex -> this.builder.globals().readField(
                    this.convertCodeTree(objIndex.obj()),
                    LiteralItem.string(objIndex.field())
            );
            case CodeTree.Negate negate -> {
                var tmp = new VariableItem("tmp.neg." + negate.hashCode(), "line");
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "x",
                        Args.byVarItems(
                                tmp,
                                this.convertCodeTree(negate.lhs()),
                                LiteralItem.number("-1")
                        )
                ));
                yield tmp;
            }
            default -> throw new RuntimeException("unknown code tree " + codeTree);
        };
    }


    private VarItem<?> convertCompare(CodeTree.Compare compare) {
        return convertCompare(
                compare,
                comparisonResult -> {
                    this.builder.appendCodeBlock(ActionBlock.setVar(
                            "=",
                            Args.byVarItems(
                                    comparisonResult,
                                    LiteralItem.number("1")
                            )
                    ));
                },
                comparisonResult -> {
                    this.builder.appendCodeBlock(ActionBlock.setVar(
                            "=",
                            Args.byVarItems(
                                    comparisonResult,
                                    LiteralItem.number("0")
                            )
                    ));
                }
        );
    }


    private VarItem<?> convertCompare(CodeTree.Compare compare, Consumer<VarItem<?>> ifTrue, Consumer<VarItem<?>> ifFalse) {
        var op = switch (compare.comparison()) {
            case EQUAL -> "=";
            case NOT_EQUAL -> "!=";
            case GREATER_THAN -> ">";
            case LESS_THAN -> "<";
            case GREATER_THAN_OR_EQ -> ">=";
            case LESS_THAN_OR_EQ -> "<=";
        };
        var comparisonResult = new VariableItem("tmp.compare_result." + compare.hashCode(), "line");
        this.builder.appendCodeBlock(ActionBlock.ifVar(op, Args.byVarItems(
                this.convertCodeTree(compare.lhs()),
                this.convertCodeTree(compare.rhs())
        )));
        this.builder.appendCodeBlock(Bracket.openNormal());
        ifTrue.accept(comparisonResult);
        this.builder.appendCodeBlock(Bracket.closeNormal());
        if (ifFalse != null) {
            this.builder.appendCodeBlock(ActionBlock.else_());
            this.builder.appendCodeBlock(Bracket.openNormal());
            ifFalse.accept(comparisonResult);
            this.builder.appendCodeBlock(Bracket.closeNormal());
        }
        return comparisonResult;
    }

    private VarItem<?> convertFlowOperation(ReconstructedFlow flow) {
        return switch (flow) {
            case ReconstructedFlow.If iff -> {
                if (iff.condition() instanceof CodeTree.Compare compare) {
                    iff.ifFalse().ifPresentOrElse(
                            onFalse -> this.convertCompare(
                                    compare,
                                    _ -> {
                                        this.convertFlowBlock(iff.ifTrue());
                                    },
                                    _ -> {
                                        this.convertFlowBlock(onFalse);
                                    }
                            ),
                            () -> this.convertCompare(
                                    compare,
                                    _ -> {
                                        this.convertFlowBlock(iff.ifTrue());
                                    },
                                    null
                            )
                    );
                } else {
                    var result = this.convertCodeTree(iff.condition());
                    this.builder.appendCodeBlock(ActionBlock.ifVar(
                            "=",
                            Args.byVarItems(result, LiteralItem.number("1"))
                    ));
                    this.builder.appendCodeBlock(Bracket.openNormal());
                    this.convertFlowBlock(iff.ifTrue());
                    this.builder.appendCodeBlock(Bracket.closeNormal());

                    iff.ifFalse().ifPresent(falseBlock -> {
                        this.builder.appendCodeBlock(ActionBlock.else_());
                        this.builder.appendCodeBlock(Bracket.openNormal());
                        this.convertFlowBlock(falseBlock);
                        this.builder.appendCodeBlock(Bracket.closeNormal());
                    });
                }
                yield LiteralItem.number("0");
            }
            case ReconstructedFlow.LoopForever loopForever -> {
                this.builder.appendCodeBlock(ActionBlock.repeat("Forever", Args.byVarItems()));
                this.builder.appendCodeBlock(Bracket.openRepeat());
                this.convertFlowBlock(loopForever.block());
                this.builder.appendCodeBlock(Bracket.closeRepeat());
                yield LiteralItem.number("0");
            }
            case ReconstructedFlow.While while_ -> {
                if (while_.condition() instanceof CodeTree.Compare(
                        ComparisonType comparison, CodeTree lhs, CodeTree rhs
                )) {
                    var op = switch (comparison) {
                        case EQUAL -> "=";
                        case NOT_EQUAL -> "!=";
                        case GREATER_THAN -> ">";
                        case LESS_THAN -> "<";
                        case GREATER_THAN_OR_EQ -> ">=";
                        case LESS_THAN_OR_EQ -> "<=";
                    };
                    this.builder.appendCodeBlock(ActionBlock.repeat("While", op, Args.byVarItems(
                            this.convertCodeTree(lhs),
                            this.convertCodeTree(rhs)
                    )));
                } else {
                    var result = this.convertCodeTree(while_.condition());
                    this.builder.appendCodeBlock(ActionBlock.repeat(
                            "While",
                            "=",
                            Args.byVarItems(result, LiteralItem.number("1"))
                    ));
                }
                this.builder.appendCodeBlock(Bracket.openRepeat());
                this.convertFlowBlock(while_.block());
                this.builder.appendCodeBlock(Bracket.closeRepeat());
                yield LiteralItem.number("0");
            }
            case ReconstructedFlow.SubroutineSafeHint hint -> {
                yield LiteralItem.number("0");
            }
            default -> throw new RuntimeException("unknown flow " + flow);
        };
    }

    private VarItem<?> convertBinOp(CodeTree.BinOp add) {
        var variable = new VariableItem("tmp.binop." + add.hashCode(), "line");
        var op = switch (add.type()) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "x";
            case DIV -> "/";
            case MOD -> "%";
            case SHR, SHL, XOR, AND, OR -> "Bitwise";
            case COMPARE_DOUBLES -> "CompareDoubles";
        };
        switch (op) {
            case "CompareDoubles" -> {
                var lhsVarItem = this.convertCodeTree(add.lhs());
                var rhsVarItem = this.convertCodeTree(add.rhs());

                var lhsVarString = "";
                if (lhsVarItem instanceof LiteralItem literalItem) {
                    lhsVarString = literalItem.value();
                }
                if (lhsVarItem instanceof VariableItem variableItem) {
                    lhsVarString = "%var(" + variableItem.name() + ")";
                }

                var rhsVarString = "";
                if (rhsVarItem instanceof LiteralItem literalItem) {
                    rhsVarString = literalItem.value();
                }
                if (rhsVarItem instanceof VariableItem variableItem) {
                    rhsVarString = "%var(" + variableItem.name() + ")";
                }

                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "ClampNumber",
                        Args.byVarItems(
                                variable,
                                LiteralItem.number("%math(" + lhsVarString + "-" + rhsVarString + "*1000)"),
                                LiteralItem.number("-1"),
                                LiteralItem.number("1")
                        )
                ));

            }
            case "Bitwise" -> {
                var bitwiseTag = switch (add.type()) {
                    case SHR -> ">>";
                    case SHL -> "<<";
                    case XOR -> "^";
                    case AND -> "&";
                    case OR -> "|";
                    default -> throw new IllegalStateException("How?");
                };
                this.builder.appendCodeBlock(
                        ActionBlock.setVar(
                                        op,
                                        Args.byVarItems(
                                                variable,
                                                this.convertCodeTree(add.lhs()),
                                                this.convertCodeTree(add.rhs())
                                        )
                                )
                                .storeTagInSlot(26, "Operator", bitwiseTag)
                                .storeTagInSlot(25, "Precision", "Default")
                );
            }
            default -> {
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        op,
                        Args.byVarItems(
                                variable,
                                this.convertCodeTree(add.lhs()),
                                this.convertCodeTree(add.rhs())
                        )
                ));
            }
        }

        return variable;
    }

    private VarItem<?> convertInvoke(CodeTree.Invoke invoke) {
        for (var handler : InvokeHandler.INVOKE_HANDLERS) {
            var result = handler.tryRewrite(invoke).orElse(null);
            if (result != null) {
                return result.apply(this);
            }
        }

        List<VarItem<?>> params = new ArrayList<VarItem<?>>();
        for (var subp : invoke.args()) {
            params.add(this.convertCodeTree(subp));
        }
        var returnVariable = new VariableItem("tmp.ret_result." + invoke.hashCode(), "line");
        if (!invoke.methodTypeDesc().returnType().equals(ClassDesc.ofDescriptor("V"))) {
            params.addFirst(returnVariable);
        }
        var outline = invoke.outline();
        var ownedClass = this.builder.graph().classByEntry(invoke.classEntry());
        switch (invoke.style()) {
            case STATIC, VIRTUAL_EXACT, DYNAMIC_CALL_SITE -> {
                this.builder.appendCodeBlock(ActionBlock.callFunction(
                        this.builder.graph().generateFunctionCallName(
                                invoke.classEntry(),
                                outline
                        ),
                        this.builder.locals().functionCallParams(params)
                ));
            }
            case VIRTUAL_INTERFACE, VIRTUAL_OVERRIDABLE -> {
                int searchIdx = 1;
                if (outline.typeDesc().returnType().equals(ClassDesc.ofDescriptor("V"))) {
                    searchIdx = 0;
                }
                if (params.get(searchIdx) instanceof VariableItem dispatchParameter) {
                    boolean guaranteedNative = false;
                    for (var attribute : ownedClass.attributes()) {
                        if (attribute instanceof RuntimeVisibleAnnotationsAttribute annotationAttribute) {
                            for (var annotation : annotationAttribute.annotations()) {
                                if (annotation.className().equalsString("Ldiamondfire/internal/annotation/NativeValue;")) {
                                    guaranteedNative = true;
                                }
                            }
                        }
                    }
                    if (guaranteedNative) {
                        this.builder.appendCodeBlock(ActionBlock.callFunction(
                                this.builder.graph().generateFunctionCallName(
                                        invoke.classEntry(),
                                        outline
                                ),
                                this.builder.locals().functionCallParams(params)
                        ));
                    } else {
                        this.builder.globals().invokeVirtual(
                                dispatchParameter,
                                outline,
                                this.builder.locals().functionCallParams(params)
                        );
                    }
                } else {
                    throw new RuntimeException("unreachable");
                }
            }
        }
        return returnVariable;
    }
}
