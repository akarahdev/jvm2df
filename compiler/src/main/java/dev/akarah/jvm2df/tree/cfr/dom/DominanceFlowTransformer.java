package dev.akarah.jvm2df.tree.cfr.dom;

import dev.akarah.jvm2df.tree.cfg.BasicBlock;
import dev.akarah.jvm2df.tree.cfr.ControlFlowTransformer;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.util.*;

/*
READER DISCLAIMER:
AI has entirely generated this class file.
I needed a better algorithm but wasn't sure how to implement it.
As such, AI has generated this for me.
This code has not been reviewed by a human yet, though I will be putting it through
extensive testing as my primary flow transformer.
Users not interested in this may fall back to the `NaiveFlowTransformer`, although that
will likely see far worse performance and exponentially higher code-block space consumption.
 */

public class DominanceFlowTransformer implements ControlFlowTransformer {
    private List<BasicBlock> basicBlocks;
    private final Map<Integer, BasicBlock> blocksByOffset = new HashMap<>();
    private final Map<BasicBlock, Set<BasicBlock>> successors = new HashMap<>();
    private final Map<BasicBlock, Set<BasicBlock>> predecessors = new HashMap<>();
    private final Map<BasicBlock, BasicBlock> idom = new HashMap<>();
    private final Map<BasicBlock, List<BasicBlock>> dominanceTree = new HashMap<>();
    private final Set<BasicBlock> visitedBlocks = new HashSet<>();
    private final Set<BasicBlock> loopHeaders = new HashSet<>();

    private void buildCfg() {
        for (var block : basicBlocks) {
            successors.put(block, new HashSet<>());
            predecessors.putIfAbsent(block, new HashSet<>());

            var term = block.terminator();
            if (term instanceof Terminator.Jump jump) {
                addEdge(block, jump.target());
            } else if (term instanceof Terminator.BranchIf branch) {
                addEdge(block, branch.ifTrue());
                addEdge(block, branch.ifFalse());
            }
        }
    }

    private void addEdge(BasicBlock from, int targetOffset) {
        var to = blocksByOffset.get(targetOffset);
        if (to == null) return;
        successors.get(from).add(to);
        predecessors.computeIfAbsent(to, _ -> new HashSet<>()).add(from);
    }

    private void computeDominators() {
        if (basicBlocks.isEmpty()) return;
        var entry = basicBlocks.getFirst();

        List<BasicBlock> rpo = computeRPO(entry);
        Map<BasicBlock, Integer> rpoIndex = new HashMap<>();
        for (int i = 0; i < rpo.size(); i++) {
            rpoIndex.put(rpo.get(i), i);
        }

        idom.put(entry, entry);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (var b : rpo) {
                if (b == entry) continue;

                BasicBlock newIdom = null;
                var preds = predecessors.get(b);
                if (preds != null) {
                    for (var p : preds) {
                        if (idom.containsKey(p)) {
                            if (newIdom == null) {
                                newIdom = p;
                            } else {
                                newIdom = intersect(p, newIdom, rpoIndex);
                            }
                        }
                    }
                }

                if (idom.get(b) != newIdom) {
                    idom.put(b, newIdom);
                    changed = true;
                }
            }
        }

        for (var block : basicBlocks) {
            var parent = idom.get(block);
            if (parent != null && parent != block) {
                dominanceTree.computeIfAbsent(parent, _ -> new ArrayList<>()).add(block);
            }
        }
    }

    private BasicBlock intersect(BasicBlock b1, BasicBlock b2, Map<BasicBlock, Integer> rpoIndex) {
        var finger1 = b1;
        var finger2 = b2;
        while (finger1 != finger2) {
            while (rpoIndex.get(finger1) > rpoIndex.get(finger2)) {
                finger1 = idom.get(finger1);
            }
            while (rpoIndex.get(finger2) > rpoIndex.get(finger1)) {
                finger2 = idom.get(finger2);
            }
        }
        return finger1;
    }

    private List<BasicBlock> computeRPO(BasicBlock entry) {
        List<BasicBlock> postOrder = new ArrayList<>();
        Set<BasicBlock> visited = new HashSet<>();
        dfsPostOrder(entry, visited, postOrder);
        Collections.reverse(postOrder);
        return postOrder;
    }

    private void dfsPostOrder(BasicBlock b, Set<BasicBlock> visited, List<BasicBlock> postOrder) {
        visited.add(b);
        var succs = successors.get(b);
        if (succs != null) {
            var sortedSuccs = new ArrayList<>(succs);
            sortedSuccs.sort(Comparator.comparingInt(BasicBlock::offset).reversed());
            for (var succ : sortedSuccs) {
                if (!visited.contains(succ)) {
                    dfsPostOrder(succ, visited, postOrder);
                }
            }
        }
        postOrder.add(b);
    }

    @Override
    public FlowBlock convert(List<BasicBlock> basicBlocks) {
        this.basicBlocks = basicBlocks;
        for (var block : basicBlocks) {
            blocksByOffset.put(block.offset(), block);
        }
        buildCfg();
        computeDominators();

        if (basicBlocks.isEmpty()) return new FlowBlock(new ArrayList<>());
        visitedBlocks.clear();
        loopHeaders.clear();
        return reconstruct(basicBlocks.getFirst(), null);
    }

    private FlowBlock reconstruct(BasicBlock block, BasicBlock stopAt) {
        if (block == null || block == stopAt || visitedBlocks.contains(block)) {
            return new FlowBlock(new ArrayList<>());
        }

        if (isLoopHeader(block) && !loopHeaders.contains(block)) {
            loopHeaders.add(block);
            BasicBlock exit = findLoopExit(block);
            
            FlowBlock body = reconstructLoopBody(block, exit);
            
            List<CodeTree> statements = new ArrayList<>();
            statements.add(new CodeTree.ExecuteFlow(new ReconstructedFlow.LoopForever(body)));
            
            if (exit != null && exit != stopAt) {
                statements.addAll(reconstruct(exit, stopAt).statements());
            }
            return new FlowBlock(statements);
        }

        visitedBlocks.add(block);
        List<CodeTree> statements = new ArrayList<>();

        var blockStatements = block.statements();
        for (int i = 0; i < blockStatements.size() - 1; i++) {
            statements.add(blockStatements.get(i));
        }

        var term = block.terminator();
        if (term instanceof Terminator.Jump jump) {
            var target = blocksByOffset.get(jump.target());
            if (target != null && !isBackEdge(block, target) && target != stopAt) {
                statements.addAll(reconstruct(target, stopAt).statements());
            }
        } else if (term instanceof Terminator.BranchIf branch) {
            var ifTrue = blocksByOffset.get(branch.ifTrue());
            var ifFalse = blocksByOffset.get(branch.ifFalse());

            BasicBlock merge = findMergePoint(block);

            statements.add(new CodeTree.ExecuteFlow(new ReconstructedFlow.If(
                    branch.operand(),
                    reconstruct(ifTrue, merge),
                    Optional.of(reconstruct(ifFalse, merge))
            )));

            if (merge != null && merge != stopAt) {
                statements.addAll(reconstruct(merge, stopAt).statements());
            }
        } else if (term instanceof Terminator.ReturnValue || term instanceof Terminator.ReturnVoid || term instanceof Terminator.Unreachable) {
            statements.add(term);
        }

        return new FlowBlock(statements);
    }

    private FlowBlock reconstructLoopBody(BasicBlock header, BasicBlock exit) {
        // Special reconstruction for loop body that allows entering the header
        List<CodeTree> statements = new ArrayList<>();
        var blockStatements = header.statements();
        for (int i = 0; i < blockStatements.size() - 1; i++) {
            statements.add(blockStatements.get(i));
        }

        var term = header.terminator();
        if (term instanceof Terminator.Jump jump) {
            var target = blocksByOffset.get(jump.target());
            if (target != null && target != header && target != exit) {
                statements.addAll(reconstruct(target, exit).statements());
            }
        } else if (term instanceof Terminator.BranchIf branch) {
            var ifTrue = blocksByOffset.get(branch.ifTrue());
            var ifFalse = blocksByOffset.get(branch.ifFalse());

            BasicBlock merge = findMergePoint(header);
            if (merge == exit) merge = null;

            statements.add(new CodeTree.ExecuteFlow(new ReconstructedFlow.If(
                    branch.operand(),
                    (ifTrue == header) ? new FlowBlock(new ArrayList<>()) : reconstruct(ifTrue, merge == null ? exit : merge),
                    Optional.of((ifFalse == header) ? new FlowBlock(new ArrayList<>()) : reconstruct(ifFalse, merge == null ? exit : merge))
            )));

            if (merge != null && merge != exit) {
                statements.addAll(reconstruct(merge, exit).statements());
            }
        } else if (term instanceof Terminator.ReturnValue || term instanceof Terminator.ReturnVoid || term instanceof Terminator.Unreachable) {
            statements.add(term);
        }
        return new FlowBlock(statements);
    }

    private boolean isBackEdge(BasicBlock from, BasicBlock to) {
        return dominates(to, from);
    }

    private boolean dominates(BasicBlock a, BasicBlock b) {
        if (a == null || b == null) return false;
        var curr = b;
        while (curr != null) {
            if (curr == a) return true;
            var next = idom.get(curr);
            if (next == curr || next == null) break;
            curr = next;
        }
        return false;
    }

    private boolean isLoopHeader(BasicBlock b) {
        var preds = predecessors.get(b);
        if (preds == null) return false;
        for (var p : preds) {
            if (dominates(b, p)) return true;
        }
        return false;
    }

    private BasicBlock findLoopExit(BasicBlock header) {
        Set<BasicBlock> loopNodes = new HashSet<>();
        var preds = predecessors.get(header);
        if (preds != null) {
            for (var p : preds) {
                if (dominates(header, p)) {
                    collectLoopNodes(p, header, loopNodes);
                }
            }
        }
        loopNodes.add(header);

        for (var node : loopNodes) {
            var succs = successors.get(node);
            if (succs != null) {
                for (var succ : succs) {
                    if (!loopNodes.contains(succ)) {
                        return succ;
                    }
                }
            }
        }
        return null;
    }

    private void collectLoopNodes(BasicBlock n, BasicBlock header, Set<BasicBlock> nodes) {
        if (n == header || !nodes.add(n)) return;
        var preds = predecessors.get(n);
        if (preds != null) {
            for (var p : preds) {
                if (dominates(header, p)) {
                    collectLoopNodes(p, header, nodes);
                }
            }
        }
    }

    private BasicBlock findMergePoint(BasicBlock header) {
        var succs = successors.get(header);
        if (succs == null || succs.size() < 2) return null;

        List<BasicBlock> children = dominanceTree.getOrDefault(header, List.of());
        for (var child : children) {
            var preds = predecessors.get(child);
            if (preds != null && preds.size() >= 2) {
                int reachableCount = 0;
                for (var s : succs) {
                    if (isReachable(s, child, header)) {
                        reachableCount++;
                    }
                }
                if (reachableCount >= 2) return child;
            }
        }

        var it = succs.iterator();
        var s1 = it.next();
        var s2 = it.next();
        if (isReachable(s1, s2, header)) return s2;
        if (isReachable(s2, s1, header)) return s1;

        return null;
    }

    private boolean isReachable(BasicBlock start, BasicBlock target, BasicBlock avoid) {
        if (start == target) return true;
        Queue<BasicBlock> queue = new LinkedList<>();
        queue.add(start);
        Set<BasicBlock> seen = new HashSet<>();
        seen.add(start);
        seen.add(avoid);

        while (!queue.isEmpty()) {
            var curr = queue.poll();
            if (curr == target) return true;

            var succs = successors.get(curr);
            if (succs != null) {
                for (var succ : succs) {
                    if (!seen.contains(succ)) {
                        seen.add(succ);
                        queue.add(succ);
                    }
                }
            }
        }
        return false;
    }
}
