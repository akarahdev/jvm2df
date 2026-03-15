package dev.akarah.jvm2df.tree.df;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.Bracket;
import dev.akarah.jvm2df.codetemplate.blocks.CodeBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.Args;
import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.df.handler.InvokeHandler;
import dev.akarah.jvm2df.tree.df.strategy.global.GlobalMemoryStrategy;
import dev.akarah.jvm2df.tree.df.strategy.local.LocalMemoryStrategy;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.ComparisonType;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CodeBlockTransformer {
    LocalMemoryStrategy locals;
    GlobalMemoryStrategy globals;
    MethodModel methodModel;
    FlowBlock block;
    List<List<CodeBlock<?>>> codeLineStack;
    List<CodeLine> confirmedCodeLines;
    CompilationGraph graph;

    public void appendCodeBlock(CodeBlock<?> codeBlock) {
        this.codeLineStack.getLast().add(codeBlock);
    }

    public List<CodeBlock<?>> popFrame() {
        this.confirmedCodeLines.add(new CodeLine(this.codeLineStack.getLast()));
        return this.codeLineStack.removeLast();
    }

    public void pushFrame() {
        this.codeLineStack.add(new ArrayList<>());
    }

    public List<CodeLine> transform(FlowBlock block, MethodModel methodModel, LocalMemoryStrategy locals, GlobalMemoryStrategy globals, CompilationGraph graph) {
        this.block = block;
        this.methodModel = methodModel;
        this.codeLineStack = new ArrayList<>(new ArrayList<>());
        this.confirmedCodeLines = new ArrayList<>();
        this.locals = locals;
        this.globals = globals;
        this.locals.setup(this);
        this.globals.setup(this, locals);
        this.graph = graph;
        this.codeLineStack = new ArrayList<>();

        var params = new ArrayList<>(this.locals.functionHeadParams(this.methodModel));

        this.pushFrame();
        var parentalName = methodModel.parent()
                .flatMap(ClassModel::superclass)
                .map(ClassEntry::asInternalName)
                .orElse("java/lang/Object");

        switch (parentalName) {
            case "diamondfire/event/PlayerEventHandler" -> {
                if (methodModel.methodName().equalsString("<init>") || methodModel.methodName().equalsString("<clinit>")) {
                    this.popFrame();
                    return List.of();
                }
                this.appendCodeBlock(ActionBlock.playerEvent(methodModel.methodName().stringValue()));
            }
            case "diamondfire/event/EntityEventHandler" -> {
                if (methodModel.methodName().equalsString("<init>") || methodModel.methodName().equalsString("<clinit>")) {
                    this.popFrame();
                    return List.of();
                }
                this.appendCodeBlock(ActionBlock.entityEvent(methodModel.methodName().stringValue()));
            }
            case "diamondfire/event/GameEventHandler" -> {
                if (methodModel.methodName().equalsString("<init>") || methodModel.methodName().equalsString("<clinit>")) {
                    this.popFrame();
                    return List.of();
                }
                this.appendCodeBlock(ActionBlock.gameEvent(methodModel.methodName().stringValue()));
            }
            default -> {
                this.appendCodeBlock(ActionBlock.function(
                        methodModel.parent().orElseThrow().thisClass().asInternalName()
                                + "#"
                                + methodModel.methodName().stringValue()
                                + methodModel.methodTypeSymbol().descriptorString(),
                        params
                ));
            }
        }

        this.convertFlowBlock(this.block);
        this.popFrame();
        return this.confirmedCodeLines;
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
                this.appendCodeBlock(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(this.locals.referenceLocal(idx), this.convertCodeTree(value))
                ));
                yield LiteralItem.number("0");
            }
            case CodeTree.LoadLocal(int idx) -> this.locals.referenceLocal(idx);
            case CodeTree.ExecuteFlow(ReconstructedFlow flow) -> this.convertFlowOperation(flow);
            case Terminator.ReturnVoid _ -> {
                this.locals.setResultAndReturn(LiteralItem.number("0"));
                yield LiteralItem.number("0");
            }
            case Terminator.ReturnValue ret -> {
                this.locals.setResultAndReturn(this.convertCodeTree(ret.code()));
                yield LiteralItem.number("0");
            }
            case Terminator.Break _ -> {
                this.appendCodeBlock(ActionBlock.control("StopRepeat", Args.byVarItems()));
                yield LiteralItem.number("0");
            }
            case Terminator.Continue _ -> {
                this.appendCodeBlock(ActionBlock.control("Skip", Args.byVarItems()));
                yield LiteralItem.number("0");
            }
            case CodeTree.Invoke invoke -> this.convertInvoke(invoke);
            case CodeTree.Compare compare -> this.convertCompare(compare);
            case CodeTree.BinOp binOp -> this.convertBinOp(binOp);
            case CodeTree.IncrementLocal inc -> {
                this.appendCodeBlock(ActionBlock.setVar(
                        "+=",
                        Args.byVarItems(
                                this.locals.referenceLocal(inc.idx()),
                                this.convertCodeTree(inc.value())
                        )
                ));
                yield this.locals.referenceLocal(inc.idx());
            }
            case CodeTree.ArrayNew arrayNew -> {
                var alloc = this.globals.allocate();
                this.globals.setField(
                        alloc,
                        LiteralItem.string("length"),
                        this.convertCodeTree(arrayNew.size())
                );
                yield alloc;
            }
            case CodeTree.ArrayStore arrayStore -> {
                var array = this.convertCodeTree(arrayStore.list());
                this.globals.setField(
                        array,
                        this.convertCodeTree(arrayStore.index()),
                        this.convertCodeTree(arrayStore.value())
                );
                yield array;
            }
            case CodeTree.ArrayIndex arrayIndex -> this.globals.readField(
                    this.convertCodeTree(arrayIndex.list()),
                    this.convertCodeTree(arrayIndex.index())
            );
            case CodeTree.ArrayLength arrayLength -> this.globals.readField(
                    this.convertCodeTree(arrayLength.list()),
                    LiteralItem.string("length")
            );
            case CodeTree.ObjectNew objectNew -> {
                var alloc = this.globals.allocate();
                this.globals.setField(
                        alloc,
                        LiteralItem.string("class"),
                        LiteralItem.string(objectNew.clazz())
                );
                yield alloc;
            }
            case CodeTree.ObjectSetStatic(String clazz, String field, CodeTree value) -> {
                this.globals.setStaticField(clazz, field, this.convertCodeTree(value));
                yield LiteralItem.number("0");
            }
            case CodeTree.ObjectGetStatic(String clazz, String field) -> this.globals.readStaticField(clazz, field);
            case CodeTree.ObjectSetField objStore -> {
                var array = this.convertCodeTree(objStore.obj());
                this.globals.setField(
                        array,
                        LiteralItem.string(objStore.field()),
                        this.convertCodeTree(objStore.value())
                );
                yield array;
            }
            case CodeTree.ObjectGetField objIndex -> this.globals.readField(
                    this.convertCodeTree(objIndex.obj()),
                    LiteralItem.string(objIndex.field())
            );
            case CodeTree.Negate negate -> {
                var tmp = new VariableItem("tmp.neg." + negate.hashCode(), "line");
                this.appendCodeBlock(ActionBlock.setVar(
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
                    this.appendCodeBlock(ActionBlock.setVar(
                            "=",
                            Args.byVarItems(
                                    comparisonResult,
                                    LiteralItem.number("1")
                            )
                    ));
                },
                comparisonResult -> {
                    this.appendCodeBlock(ActionBlock.setVar(
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
        this.appendCodeBlock(ActionBlock.ifVar(op, Args.byVarItems(
                this.convertCodeTree(compare.lhs()),
                this.convertCodeTree(compare.rhs())
        )));
        this.appendCodeBlock(Bracket.openNormal());
        ifTrue.accept(comparisonResult);
        this.appendCodeBlock(Bracket.closeNormal());
        if (ifFalse != null) {
            this.appendCodeBlock(ActionBlock.else_());
            this.appendCodeBlock(Bracket.openNormal());
            ifFalse.accept(comparisonResult);
            this.appendCodeBlock(Bracket.closeNormal());
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
                    this.appendCodeBlock(ActionBlock.ifVar(
                            "=",
                            Args.byVarItems(result, LiteralItem.number("1"))
                    ));
                    this.appendCodeBlock(Bracket.openNormal());
                    this.convertFlowBlock(iff.ifTrue());
                    this.appendCodeBlock(Bracket.closeNormal());

                    iff.ifFalse().ifPresent(falseBlock -> {
                        this.appendCodeBlock(ActionBlock.else_());
                        this.appendCodeBlock(Bracket.openNormal());
                        this.convertFlowBlock(falseBlock);
                        this.appendCodeBlock(Bracket.closeNormal());
                    });
                }
                yield LiteralItem.number("0");
            }
            case ReconstructedFlow.LoopForever loopForever -> {
                this.appendCodeBlock(ActionBlock.repeat("Forever", Args.byVarItems()));
                this.appendCodeBlock(Bracket.openRepeat());
                this.convertFlowBlock(loopForever.block());
                this.appendCodeBlock(Bracket.closeRepeat());
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
                    this.appendCodeBlock(ActionBlock.repeat("While", op, Args.byVarItems(
                            this.convertCodeTree(lhs),
                            this.convertCodeTree(rhs)
                    )));
                } else {
                    var result = this.convertCodeTree(while_.condition());
                    this.appendCodeBlock(ActionBlock.repeat(
                            "While",
                            "=",
                            Args.byVarItems(result, LiteralItem.number("1"))
                    ));
                }
                this.appendCodeBlock(Bracket.openRepeat());
                this.convertFlowBlock(while_.block());
                this.appendCodeBlock(Bracket.closeRepeat());
                yield LiteralItem.number("0");
            }
            case ReconstructedFlow.SubroutineSafeHint hint -> {
                this.locals.compileSubroutineHint(hint);
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

                this.appendCodeBlock(ActionBlock.setVar(
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
                this.appendCodeBlock(
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
                this.appendCodeBlock(ActionBlock.setVar(
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
        var outline = new CompilationGraph.MethodOutline(
                invoke.descriptor().name().stringValue(),
                invoke.methodTypeDesc()
        );
        switch (invoke.style()) {
            case STATIC, VIRTUAL_EXACT, DYNAMIC_CALL_SITE -> {
                this.appendCodeBlock(ActionBlock.callFunction(
                        this.graph.generateFunctionCallName(
                                invoke.descriptor().owner(),
                                outline
                        ),
                        this.locals.functionCallParams(params)
                ));
            }
            case VIRTUAL_INTERFACE, VIRTUAL_OVERRIDABLE -> {
                int searchIdx = 1;
                if (outline.typeDesc().returnType().equals(ClassDesc.ofDescriptor("V"))) {
                    searchIdx = 0;
                }
                if (params.get(searchIdx) instanceof VariableItem dispatchParameter) {
                    this.globals.invokeVirtual(
                            dispatchParameter,
                            outline,
                            this.locals.functionCallParams(params)
                    );
                } else {
                    throw new RuntimeException("unreachable");
                }
            }
        }
        return returnVariable;
    }
}
