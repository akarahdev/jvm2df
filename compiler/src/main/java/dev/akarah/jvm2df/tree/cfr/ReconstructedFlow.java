package dev.akarah.jvm2df.tree.cfr;

import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ReconstructedFlow {
    List<FlowBlock> targets();

    record LoopForever(FlowBlock block) implements ReconstructedFlow {
        @Override
        public List<FlowBlock> targets() {
            return List.of(this.block);
        }
    }

    record SubroutineSafeHint(FlowBlock block) implements ReconstructedFlow {
        @Override
        public List<FlowBlock> targets() {
            return List.of(this.block);
        }
    }

    record While(CodeTree condition, FlowBlock block) implements ReconstructedFlow {
        @Override
        public List<FlowBlock> targets() {
            return List.of(this.block);
        }
    }

    record If(CodeTree condition, FlowBlock ifTrue, Optional<FlowBlock> ifFalse) implements ReconstructedFlow {
        @Override
        public List<FlowBlock> targets() {
            var list = new ArrayList<FlowBlock>();
            list.add(ifTrue);
            ifFalse.ifPresent(list::add);
            return list;
        }
    }
}
