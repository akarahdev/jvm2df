package dev.akarah.jvm2df.tree.df;

import dev.akarah.jvm2df.codetemplate.blocks.ActionBlock;
import dev.akarah.jvm2df.codetemplate.blocks.Bracket;
import dev.akarah.jvm2df.codetemplate.blocks.CodeBlock;
import dev.akarah.jvm2df.codetemplate.blocks.CodeLine;
import dev.akarah.jvm2df.codetemplate.items.*;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.MethodMeta;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class CodeBlockTransformer {
    MemoryStrategy strategy;
    FlowBlock block;
    MethodMeta methodMeta;
    List<CodeBlock<?>> codeBlocks;

    public CodeBlockTransformer(FlowBlock block, MethodMeta methodMeta) {
        this.block = block;
        this.methodMeta = methodMeta;
    }

    public CodeBlockTransformer withStrategy(MemoryStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public CodeLine transform() {
        this.codeBlocks = new ArrayList<>();
        var params = new ArrayList<>(
                IntStream.range(0, methodMeta.methodTypeDesc().parameterCount())
                        .mapToObj(x -> new ParameterItem("local." + x, "any", false, false))
                        .toList()
        );

        if(!methodMeta.methodTypeDesc().returnType().equals(ClassDesc.ofDescriptor("V"))) {
            params.addFirst(new ParameterItem("return", "var", false, false));
        }
        this.codeBlocks.add(ActionBlock.function(
                methodMeta.toString(),
                (List<VarItem<?>>) (Object) params
        ));
        this.convertFlowBlock(this.block);
        return new CodeLine(this.codeBlocks);
    }

    private void convertFlowBlock(FlowBlock block) {
        for(var tree : block.statements()) {
            this.convertCodeTree(tree);
        }
    }

    private VarItem<?> convertCodeTree(CodeTree codeTree) {
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
                this.codeBlocks.add(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(new VariableItem("local." + idx, "line"), this.convertCodeTree(value))
                ));
                yield LiteralItem.number("0");
            }
            case CodeTree.LoadLocal(int idx) -> new VariableItem("local." + idx, "line");
            case CodeTree.ExecuteFlow(ReconstructedFlow flow) -> this.convertFlowOperation(flow);
            case Terminator.ReturnVoid ret -> {
                this.codeBlocks.add(ActionBlock.control("Return", Args.byVarItems()));
                yield LiteralItem.number("0");
            }
            case Terminator.ReturnValue ret -> {
                this.codeBlocks.add(ActionBlock.setVar(
                        "=",
                        Args.byVarItems(
                                new VariableItem("return", "line"),
                                this.convertCodeTree(ret.code())
                        )
                ));
                this.codeBlocks.add(ActionBlock.control("Return", Args.byVarItems()));
                yield LiteralItem.number("0");
            }
            case CodeTree.Invoke invoke -> {
                var params = new ArrayList<VarItem<?>>();
                for(var subp : invoke.args()) {
                    params.add(this.convertCodeTree(subp));
                }
                var returnVariable = new VariableItem("ret_result." + invoke.hashCode(), "line");
                if(!invoke.descriptor().endsWith("V")) {
                    params.addFirst(returnVariable);
                }
                this.codeBlocks.add(ActionBlock.callFunction(
                        invoke.descriptor(),
                        params
                ));
                yield returnVariable;
            }
            case CodeTree.Compare compare -> convertCompare(
                    compare,
                    comparisonResult -> {
                        this.codeBlocks.add(ActionBlock.setVar(
                                "=",
                                Args.byVarItems(
                                        comparisonResult,
                                        LiteralItem.number("1")
                                )
                        ));
                    },
                    comparisonResult -> {
                        this.codeBlocks.add(ActionBlock.setVar(
                                "=",
                                Args.byVarItems(
                                        comparisonResult,
                                        LiteralItem.number("0")
                                )
                        ));
                    }
            );
            case CodeTree.BinOp add -> {
                var variable = new VariableItem("add." + add.hashCode(), "line");
                this.codeBlocks.add(ActionBlock.setVar(
                        "+",
                        Args.byVarItems(
                                variable,
                                this.convertCodeTree(add.lhs()),
                                this.convertCodeTree(add.rhs())
                        )
                ));
                yield variable;
            }
            case CodeTree.IncrementLocal inc -> {
                this.codeBlocks.add(ActionBlock.setVar(
                        "+=",
                        Args.byVarItems(
                                new VariableItem("local." + inc.idx(), "line"),
                                this.convertCodeTree(inc.value())
                        )
                ));
                yield new VariableItem("local." + inc.idx(), "line");
            }
            default -> throw new RuntimeException("unknown code tree " + codeTree);
        };
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
        var comparisonResult = new VariableItem("compare_result." + compare.hashCode(), "line");
        this.codeBlocks.add(ActionBlock.ifVar(op, Args.byVarItems(
                this.convertCodeTree(compare.lhs()),
                this.convertCodeTree(compare.rhs())
        )));
        this.codeBlocks.add(Bracket.openNormal());
        ifTrue.accept(comparisonResult);
        this.codeBlocks.add(Bracket.closeNormal());
        if(ifFalse != null) {
            this.codeBlocks.add(ActionBlock.else_());
            this.codeBlocks.add(Bracket.openNormal());
            ifFalse.accept(comparisonResult);
            this.codeBlocks.add(Bracket.closeNormal());
        }
        return comparisonResult;
    }

    private VarItem<?> convertFlowOperation(ReconstructedFlow flow) {
        return switch (flow) {
            case ReconstructedFlow.If iff -> {
                if(iff.condition() instanceof CodeTree.Compare compare) {
                    iff.ifFalse().ifPresentOrElse(
                            onFalse -> this.convertCompare(
                                    compare,
                                    _ -> { this.convertFlowBlock(iff.ifTrue()); },
                                    _ -> { this.convertFlowBlock(onFalse); }
                            ),
                            () -> this.convertCompare(
                                    compare,
                                    _ -> { this.convertFlowBlock(iff.ifTrue()); },
                                    null
                            )
                    );

                } else {
                    var result = this.convertCodeTree(iff.condition());
                    this.codeBlocks.add(ActionBlock.ifVar(
                            "=",
                            Args.byVarItems(result, LiteralItem.number("1"))
                    ));
                    this.codeBlocks.add(Bracket.openNormal());
                    this.convertFlowBlock(iff.ifTrue());
                    this.codeBlocks.add(Bracket.closeNormal());

                    iff.ifFalse().ifPresent(falseBlock -> {
                        this.codeBlocks.add(ActionBlock.else_());
                        this.codeBlocks.add(Bracket.openNormal());
                        this.convertFlowBlock(falseBlock);
                        this.codeBlocks.add(Bracket.closeNormal());
                    });
                }
                yield LiteralItem.number("0");
            }
            case ReconstructedFlow.LoopForever loopForever -> {
                this.codeBlocks.add(ActionBlock.repeat("Forever", Args.byVarItems()));
                this.codeBlocks.add(Bracket.openRepeat());
                this.convertFlowBlock(loopForever.block());
                this.codeBlocks.add(Bracket.closeRepeat());
                yield LiteralItem.number("0");
            }
            default -> throw new RuntimeException("unknown flow " + flow);
        };
    }
}
