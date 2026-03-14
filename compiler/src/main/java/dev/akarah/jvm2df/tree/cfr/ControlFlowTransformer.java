package dev.akarah.jvm2df.tree.cfr;

import dev.akarah.jvm2df.tree.cfg.BasicBlock;

import java.util.ArrayList;
import java.util.List;

public interface ControlFlowTransformer {
    FlowBlock convert(List<BasicBlock> basicBlocks);
}
