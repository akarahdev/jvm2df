package dev.akarah.jvm2df.tree.cfr.dom;

import dev.akarah.jvm2df.tree.cfg.BasicBlock;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.ComparisonType;
import dev.akarah.jvm2df.tree.instructions.Terminator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DominanceFlowTransformerTest {
    @Test
    void reconstructsTrailingBranchIfChainAsNestedConditionals() {
        var blocks = List.of(
                new BasicBlock(0, new ArrayList<>(List.of(
                        branch(1, 10),
                        branch(2, 20),
                        branch(3, 30)
                ))),
                new BasicBlock(10, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(1), CodeTree.Kind.PRIMITIVE),
                        new Terminator.Jump(50)
                ))),
                new BasicBlock(20, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(2), CodeTree.Kind.PRIMITIVE),
                        new Terminator.Jump(50)
                ))),
                new BasicBlock(30, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(3), CodeTree.Kind.PRIMITIVE),
                        new Terminator.Jump(50)
                ))),
                new BasicBlock(40, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(0), CodeTree.Kind.PRIMITIVE),
                        new Terminator.Jump(50)
                ))),
                new BasicBlock(50, new ArrayList<>(List.of(
                        new Terminator.ReturnVoid()
                )))
        );

        FlowBlock flow = new DominanceFlowTransformer().convert(blocks);

        assertEquals(2, flow.statements().size());
        var conditional = assertExecuteIf(flow.statements().getFirst());
        assertIsStoreLocal(conditional.ifTrue(), 1);

        var second = assertNestedIf(conditional.ifFalse().orElseThrow());
        assertIsStoreLocal(second.ifTrue(), 2);

        var third = assertNestedIf(second.ifFalse().orElseThrow());
        assertIsStoreLocal(third.ifTrue(), 3);
        assertIsStoreLocal(third.ifFalse().orElseThrow(), 0);

        assertInstanceOf(Terminator.ReturnVoid.class, flow.statements().get(1));
    }

    @Test
    void reconstructsDiamondBranchWithSinglePostMergeContinuation() {
        var blocks = List.of(
                new BasicBlock(0, new ArrayList<>(List.of(
                        branch(1, 10, 20)
                ))),
                new BasicBlock(10, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(10), CodeTree.Kind.PRIMITIVE),
                        new Terminator.Jump(30)
                ))),
                new BasicBlock(20, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(20), CodeTree.Kind.PRIMITIVE),
                        new Terminator.Jump(30)
                ))),
                new BasicBlock(30, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(2, new CodeTree.Constant(30), CodeTree.Kind.PRIMITIVE),
                        new Terminator.ReturnVoid()
                )))
        );

        FlowBlock flow = new DominanceFlowTransformer().convert(blocks);

        assertEquals(3, flow.statements().size());

        var conditional = assertExecuteIf(flow.statements().getFirst());
        assertIsStoreLocal(conditional.ifTrue(), 10);
        assertIsStoreLocal(conditional.ifFalse().orElseThrow(), 20);

        var mergeStore = assertInstanceOf(CodeTree.StoreLocal.class, flow.statements().get(1));
        assertEquals(2, mergeStore.idx());
        assertEquals(new CodeTree.Constant(30), mergeStore.value());
        assertInstanceOf(Terminator.ReturnVoid.class, flow.statements().get(2));
    }

    @Test
    void reconstructsWhileLoopByNegatingExitOnTrueBranch() {
        var blocks = List.of(
                new BasicBlock(0, new ArrayList<>(List.of(
                        new Terminator.BranchIf(
                                new CodeTree.Compare(
                                        ComparisonType.EQUAL,
                                        new CodeTree.LoadLocal(0),
                                        new CodeTree.Constant(0)
                                ),
                                20,
                                10
                        )
                ))),
                new BasicBlock(10, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(1), CodeTree.Kind.PRIMITIVE),
                        new Terminator.Jump(0)
                ))),
                new BasicBlock(20, new ArrayList<>(List.of(
                        new Terminator.ReturnVoid()
                )))
        );

        FlowBlock flow = new DominanceFlowTransformer().convert(blocks);

        assertEquals(2, flow.statements().size());

        var loop = assertExecuteWhile(flow.statements().getFirst());
        assertEquals(
                new CodeTree.Compare(
                        ComparisonType.NOT_EQUAL,
                        new CodeTree.LoadLocal(0),
                        new CodeTree.Constant(0)
                ),
                loop.condition()
        );
        assertIsStoreLocal(loop.block(), 1);

        assertInstanceOf(Terminator.ReturnVoid.class, flow.statements().get(1));
    }

    @Test
    void reconstructsLoopForeverBodyWithContinueAndBreakBranches() {
        var blocks = List.of(
                new BasicBlock(0, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(1), CodeTree.Kind.PRIMITIVE),
                        new Terminator.BranchIf(
                                new CodeTree.Compare(
                                        ComparisonType.EQUAL,
                                        new CodeTree.LoadLocal(0),
                                        new CodeTree.Constant(0)
                                ),
                                0,
                                20
                        )
                ))),
                new BasicBlock(20, new ArrayList<>(List.of(
                        new Terminator.ReturnVoid()
                )))
        );

        FlowBlock flow = new DominanceFlowTransformer().convert(blocks);

        assertEquals(2, flow.statements().size());

        var loopForever = assertExecuteLoopForever(flow.statements().getFirst());
        assertEquals(2, loopForever.block().statements().size());
        assertIsStoreLocal(loopForever.block(), 1);

        var nestedIf = assertExecuteIf(loopForever.block().statements().get(1));
        assertEquals(
                new CodeTree.Compare(
                        ComparisonType.EQUAL,
                        new CodeTree.LoadLocal(0),
                        new CodeTree.Constant(0)
                ),
                nestedIf.condition()
        );
        assertContinue(nestedIf.ifTrue());
        assertBreak(nestedIf.ifFalse().orElseThrow());

        assertInstanceOf(Terminator.ReturnVoid.class, flow.statements().get(1));
    }

    @Test
    void convertResetsAnalysisStateBetweenInvocations() {
        var transformer = new DominanceFlowTransformer();

        var first = transformer.convert(List.of(
                new BasicBlock(0, new ArrayList<>(List.of(
                        new CodeTree.StoreLocal(1, new CodeTree.Constant(1), CodeTree.Kind.PRIMITIVE),
                        new Terminator.BranchIf(
                                new CodeTree.Compare(
                                        ComparisonType.EQUAL,
                                        new CodeTree.LoadLocal(0),
                                        new CodeTree.Constant(0)
                                ),
                                0,
                                20
                        )
                ))),
                new BasicBlock(20, new ArrayList<>(List.of(
                        new Terminator.ReturnVoid()
                )))
        ));

        assertEquals(2, first.statements().size());
        assertInstanceOf(CodeTree.ExecuteFlow.class, first.statements().getFirst());

        var second = transformer.convert(List.of(
                new BasicBlock(100, new ArrayList<>(List.of(
                        new Terminator.ReturnVoid()
                )))
        ));

        assertEquals(1, second.statements().size());
        assertInstanceOf(Terminator.ReturnVoid.class, second.statements().getFirst());
    }

    private static Terminator.BranchIf branch(int value, int ifTrue) {
        return branch(value, ifTrue, 40);
    }

    private static Terminator.BranchIf branch(int value, int ifTrue, int ifFalse) {
        return new Terminator.BranchIf(
                new CodeTree.Compare(
                        ComparisonType.EQUAL,
                        new CodeTree.LoadLocal(0),
                        new CodeTree.Constant(value)
                ),
                ifTrue,
                ifFalse
        );
    }

    private static ReconstructedFlow.If assertExecuteIf(CodeTree codeTree) {
        var executeFlow = assertInstanceOf(CodeTree.ExecuteFlow.class, codeTree);
        return assertInstanceOf(ReconstructedFlow.If.class, executeFlow.flow());
    }

    private static ReconstructedFlow.If assertNestedIf(FlowBlock block) {
        assertEquals(1, block.statements().size());
        return assertExecuteIf(block.statements().getFirst());
    }

    private static ReconstructedFlow.While assertExecuteWhile(CodeTree codeTree) {
        var executeFlow = assertInstanceOf(CodeTree.ExecuteFlow.class, codeTree);
        return assertInstanceOf(ReconstructedFlow.While.class, executeFlow.flow());
    }

    private static ReconstructedFlow.LoopForever assertExecuteLoopForever(CodeTree codeTree) {
        var executeFlow = assertInstanceOf(CodeTree.ExecuteFlow.class, codeTree);
        return assertInstanceOf(ReconstructedFlow.LoopForever.class, executeFlow.flow());
    }

    private static void assertContinue(FlowBlock block) {
        assertEquals(1, block.statements().size());
        assertInstanceOf(Terminator.Continue.class, block.statements().getFirst());
    }

    private static void assertBreak(FlowBlock block) {
        assertEquals(1, block.statements().size());
        assertInstanceOf(Terminator.Break.class, block.statements().getFirst());
    }

    private static void assertIsStoreLocal(FlowBlock block, int value) {
        assertEquals(1, block.statements().size());
        var store = assertInstanceOf(CodeTree.StoreLocal.class, block.statements().getFirst());
        assertEquals(1, store.idx());
        assertEquals(new CodeTree.Constant(value), store.value());
    }
}

