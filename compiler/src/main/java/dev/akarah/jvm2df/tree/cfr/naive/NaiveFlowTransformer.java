package dev.akarah.jvm2df.tree.cfr.naive;

import dev.akarah.jvm2df.tree.cfg.BasicBlock;
import dev.akarah.jvm2df.tree.cfr.ControlFlowTransformer;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.ComparisonType;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NaiveFlowTransformer implements ControlFlowTransformer {
    public static final int LABEL_VARIABLE = Integer.MAX_VALUE;

    @Override
    public FlowBlock convert(List<BasicBlock> basicBlocks) {
        var mainLoop = new FlowBlock(new ArrayList<>());

        for (var block : basicBlocks) {
            mainLoop.statements().add(new CodeTree.ExecuteFlow(
                    new ReconstructedFlow.If(
                            new CodeTree.Compare(
                                    ComparisonType.EQUAL,
                                    new CodeTree.LoadLocal(LABEL_VARIABLE),
                                    new CodeTree.Constant(block.offset())
                            ),
                            block.mapToFlow(this::instructionMapper),
                            Optional.empty()
                    )
            ));
        }

        return FlowBlock.by(
                new CodeTree.ExecuteFlow(new ReconstructedFlow.LoopForever(
                        mainLoop
                ))
        );
    }

    private CodeTree instructionMapper(Terminator terminator) {
        return switch (terminator) {
            case Terminator.Jump(int target) -> new CodeTree.StoreLocal(LABEL_VARIABLE, new CodeTree.Constant(target));
            case Terminator.BranchIf(CodeTree condition, int ifTrue, int ifFalse) -> new CodeTree.ExecuteFlow(
                    new ReconstructedFlow.If(
                            condition,
                            FlowBlock.by(
                                    new CodeTree.StoreLocal(LABEL_VARIABLE, new CodeTree.Constant(ifTrue))
                            ),
                            Optional.of(FlowBlock.by(
                                    new CodeTree.StoreLocal(LABEL_VARIABLE, new CodeTree.Constant(ifFalse))
                            ))
                    )
            );
            case Terminator.ReturnValue value -> value;
            case Terminator.ReturnVoid value -> value;
            default -> throw new IllegalStateException("Unexpected value: " + terminator);
        };
    }
}
