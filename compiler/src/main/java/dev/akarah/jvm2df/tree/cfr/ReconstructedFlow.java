package dev.akarah.jvm2df.tree.cfr;

import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;

public interface ReconstructedFlow {
    record LoopForever(FlowBlock block) implements ReconstructedFlow {}
    record SubroutineSafeHint(FlowBlock block) implements ReconstructedFlow {}
    record While(CodeTree condition, FlowBlock block) implements ReconstructedFlow {}
    record If(CodeTree condition, FlowBlock ifTrue, Optional<FlowBlock> ifFalse) implements ReconstructedFlow {}
}
