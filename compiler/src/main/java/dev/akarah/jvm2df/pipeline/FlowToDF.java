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
import dev.akarah.jvm2df.tree.df.VarPattern;
import dev.akarah.jvm2df.tree.df.handler.dynamic.InvokeDynamicHandler;
import dev.akarah.jvm2df.tree.df.handler.statics.InvokeHandler;
import dev.akarah.jvm2df.tree.df.strategy.global.GlobalMemoryStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.ComparisonType;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class FlowToDF {
    CodeLineBuilder builder;
    Set<Integer> storedLocals;

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

    private Set<Integer> storedLocals(FlowBlock flowBlock) {
        var set = new HashSet<Integer>();
        for (var statement : flowBlock.statements()) {
            switch (statement) {
                case CodeTree.StoreLocal(int idx, CodeTree value, CodeTree.Kind kind) -> {
                    set.add(idx);
                }
                case CodeTree.ExecuteFlow(ReconstructedFlow flow) -> {
                    for (var block : flow.targets()) {
                        set.addAll(this.storedLocals(block));
                    }
                }
                default -> {
                }
            }
        }
        return set;
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
        this.storedLocals = this.storedLocals(block);

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
            case "java/lang/Thread" -> {
                if (methodModel.methodName().equalsString("run")) {
                    this.builder.appendCodeBlock(ActionBlock.process(
                            methodModel.parent().orElseThrow().thisClass().asSymbol().descriptorString()
                                    + "#"
                                    + methodModel.methodName().stringValue()
                                    + methodModel.methodTypeSymbol().descriptorString(),
                            params
                    ));
                } else {
                    this.builder.appendCodeBlock(ActionBlock.function(
                            methodModel.parent().orElseThrow().thisClass().asSymbol().descriptorString()
                                    + "#"
                                    + methodModel.methodName().stringValue()
                                    + methodModel.methodTypeSymbol().descriptorString(),
                            params
                    ));
                }
            }
            default -> {
                this.builder.appendCodeBlock(ActionBlock.function(
                        methodModel.parent().orElseThrow().thisClass().asSymbol().descriptorString()
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
                case ClassDesc classDesc -> {
                    var alloc = this.builder().globals().allocate();
                    this.builder().globals().setField(
                            alloc,
                            LiteralItem.string("class"),
                            LiteralItem.string("Ljava/lang/Class;")
                    );
                    this.builder().globals().setField(
                            alloc,
                            LiteralItem.string("descriptor"),
                            LiteralItem.string(classDesc.descriptorString())
                    );
                    yield alloc;
                }
                default -> {
                    throw new RuntimeException("I can't handle this right now :(");
                }
            };
            case CodeTree.StoreLocal(int idx, CodeTree value, CodeTree.Kind kind) -> {
                if (kind.equals(CodeTree.Kind.REFERENCE) && idx <= 65535) {
                    this.builder().globals().dereference(this.builder.locals().referenceLocal(idx));
                }
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(this.builder.locals().referenceLocal(idx), this.convertCodeTree(value))
                ));
                if (kind.equals(CodeTree.Kind.REFERENCE) && idx <= 65535) {
                    this.builder().globals().reference(this.builder.locals().referenceLocal(idx));
                }
                yield LiteralItem.number("0");
            }
            case CodeTree.LoadLocal(int idx) -> this.builder.locals().referenceLocal(idx);
            case CodeTree.ExecuteFlow(ReconstructedFlow flow) -> this.convertFlowOperation(flow);
            case Terminator.ReturnVoid _ -> {
                for (var elem : this.storedLocals) {
                    if (elem <= 65535) {
                        this.builder().globals().cleanup(elem);
                    }
                }
                this.builder.locals().setResultAndReturn(null);
                yield LiteralItem.number("0");
            }
            case Terminator.ReturnValue ret -> {
                var val = this.convertCodeTree(ret.code());

                for (var elem : this.storedLocals) {
                    if (elem <= 65535) {
                        this.builder().globals().cleanup(elem);
                    }
                }
                this.builder.locals().setResultAndReturn(val);
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
            case CodeTree.ObjectSetStatic(String clazz, String field, CodeTree value, CodeTree.Kind kind) -> {
                if (kind.equals(CodeTree.Kind.REFERENCE)) {
                    this.builder().globals().dereference(this.builder.globals().readStaticField(clazz, field));
                }
                this.builder.globals().setStaticField(clazz, field, this.convertCodeTree(value));
                if (kind.equals(CodeTree.Kind.REFERENCE)) {
                    this.builder().globals().reference(this.builder.globals().readStaticField(clazz, field));
                }
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
                var tmp = VarPattern.temporary("neg");
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
            case CodeTree.InvokeDynamic invokeDynamic -> convertInvokeDynamic(invokeDynamic);
            case CodeTree.Wrap wrap -> {
                var tmp = VarPattern.temporary("wrap");
                var fmt = new DecimalFormat("##########################################################.###");
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "WrapNum",
                        Args.byVarItems(
                                tmp,
                                this.convertCodeTree(wrap.base()),
                                LiteralItem.number(fmt.format(wrap.min().doubleValue())),
                                LiteralItem.number(fmt.format(wrap.max().doubleValue() + 1))
                        )
                ));
                yield tmp;
            }
            case CodeTree.Round round -> {
                var tmp = VarPattern.temporary("wrap");
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        " RoundNumber ",
                        Args.byVarItems(
                                tmp,
                                this.convertCodeTree(round.base())
                        )
                ).storeTagInSlot(26, "Round Mode", "Floor"));
                yield tmp;
            }
            case CodeTree.IsInstanceOf isInstanceOf -> {
                var base = this.convertCodeTree(isInstanceOf.value());
                var clazz = this.builder().globals().readClass(base);
                var out = VarPattern.temporary("isinstanceof");
                var params = buildSuperClassParams(isInstanceOf.descriptor(), clazz);
                this.builder.appendCodeBlock(ActionBlock.ifVar(
                        "=",
                        Args.byVarItems(params)
                ));
                this.builder.appendCodeBlock(Bracket.openNormal());
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(
                                out,
                                LiteralItem.number("1")
                        )
                ));
                this.builder.appendCodeBlock(Bracket.closeNormal());
                this.builder.appendCodeBlock(ActionBlock.else_());
                this.builder.appendCodeBlock(Bracket.openNormal());
                this.builder.appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(
                                out,
                                LiteralItem.number("0")
                        )
                ));
                this.builder.appendCodeBlock(Bracket.closeNormal());
                yield out;
            }
            case CodeTree.CastValueTo castValueTo -> {
                var base = this.convertCodeTree(castValueTo.base());
                var clazz = (VariableItem) this.builder().globals().readClass(base);
                var params = buildSuperClassParams(castValueTo.descriptor(), clazz);
                this.builder.appendCodeBlock(ActionBlock.ifVar(
                        "!=",
                        Args.byVarItems(params)
                ));
                this.builder.appendCodeBlock(Bracket.openNormal());
                this.builder.appendCodeBlock(ActionBlock.control(
                                "PrintDebug",
                                Args.byVarItems(
                                        LiteralItem.text("Failed to cast object of type %var("
                                                + clazz.name()
                                                + ") to class "
                                                + castValueTo.descriptor().asSymbol().descriptorString())
                                )
                        ).storeTagInSlot(26, "Message Style", "Error")
                        .storeTagInSlot(25, "Sound", "Error")
                        .storeTagInSlot(24, "Highlighting", "Error")
                        .storeTagInSlot(23, "Text Value Merging", "No Spaces")
                        .storeTagInSlot(22, "Permission", "Developer"));
                this.builder.appendCodeBlock(ActionBlock.control(
                        "End",
                        Args.byVarItems()
                ));
                this.builder.appendCodeBlock(Bracket.closeNormal());
                yield base;
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
        var comparisonResult = VarPattern.temporary("compare_result");
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
                this.builder.appendCodeBlock(ActionBlock.repeat("Forever", Args.byVarItems()));
                this.builder.appendCodeBlock(Bracket.openRepeat());
                if (while_.condition() instanceof CodeTree.Compare(
                        ComparisonType comparison, CodeTree lhs, CodeTree rhs
                )) {
                    var op = switch (comparison) {
                        case EQUAL -> "!=";
                        case NOT_EQUAL -> "=";
                        case GREATER_THAN -> "<=";
                        case LESS_THAN -> ">=";
                        case GREATER_THAN_OR_EQ -> "<";
                        case LESS_THAN_OR_EQ -> ">";
                    };
                    this.builder.appendCodeBlock(ActionBlock.ifVar(op, Args.byVarItems(
                            this.convertCodeTree(lhs),
                            this.convertCodeTree(rhs)
                    )));
                    this.builder.appendCodeBlock(Bracket.openNormal());
                    this.builder.appendCodeBlock(ActionBlock.control("StopRepeat", Args.byVarItems()));
                    this.builder.appendCodeBlock(Bracket.closeNormal());
                } else {
                    var result = this.convertCodeTree(while_.condition());
                    this.builder.appendCodeBlock(ActionBlock.ifVar(
                            "!=",
                            Args.byVarItems(result, LiteralItem.number("1"))
                    ));
                    this.builder.appendCodeBlock(Bracket.openNormal());
                    this.builder.appendCodeBlock(ActionBlock.control("StopRepeat", Args.byVarItems()));
                    this.builder.appendCodeBlock(Bracket.closeNormal());
                }
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
        var variable = VarPattern.temporary("binop");
        var op = switch (add.type()) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "x";
            case DIV -> "/";
            case MOD -> "%";
            case SHR, SHL, XOR, AND, OR -> "Bitwise";
            case COMPARE_NUMBERS -> "CompareNumbers";
        };
        switch (op) {
            case "CompareNumbers" -> {
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
                                LiteralItem.number("%math(%math(" + lhsVarString + "-" + rhsVarString + ")*1000)"),
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
        var returnVariable = VarPattern.temporary("result");
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
                this.builder.globals().invokeVirtual(
                        params.get(searchIdx),
                        outline,
                        this.builder.locals().functionCallParams(params),
                        false
                );

            }
        }
        return returnVariable;
    }

    private VarItem<?> convertInvokeDynamic(CodeTree.InvokeDynamic invoke) {
        for (var handler : InvokeDynamicHandler.INVOKE_HANDLERS) {
            var rewrite = handler.tryRewrite(invoke);
            if (rewrite.isPresent()) {
                return rewrite.get().apply(this);
            }
        }
        throw new RuntimeException("I don't understand this invokedynamic " + invoke);
    }

    // TODO: refactor to support classes downcasting
    private ArrayList<VarItem<?>> buildSuperClassParams(ClassEntry descriptor, VarItem<?> clazz) {
        var params = new ArrayList<VarItem<?>>();
        params.add(clazz);
        params.add(LiteralItem.string(descriptor.asSymbol().descriptorString()));
        return params;
    }
}
