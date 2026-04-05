package dev.akarah.jvm2df.tree.cfr.dom;

import dev.akarah.jvm2df.tree.cfg.BasicBlock;
import dev.akarah.jvm2df.tree.cfr.ControlFlowTransformer;
import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.ComparisonType;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.util.*;
import java.util.function.Function;

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

    private record BranchChain(int startIndex, List<Terminator.BranchIf> branches) {
    }

    private void buildCfg() {
        for (var block : basicBlocks) {
            successors.put(block, new HashSet<>());
            predecessors.putIfAbsent(block, new HashSet<>());

            var term = block.terminator();
            if (term instanceof Terminator.Jump(int target)) {
                addEdge(block, target);
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
                        if (idom.containsKey(p) && rpoIndex.containsKey(p)) {
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
            Integer idx1 = rpoIndex.get(finger1);
            Integer idx2 = rpoIndex.get(finger2);
            if (idx1 == null || idx2 == null) {
                return (idx1 != null) ? finger1 : finger2;
            }

            while (idx1 > idx2) {
                var next = idom.get(finger1);
                if (next == null || next == finger1) {
                    return finger1;
                }
                finger1 = next;
                idx1 = rpoIndex.get(finger1);
                if (idx1 == null) {
                    return finger2;
                }
            }
            while (idx2 > idx1) {
                var next = idom.get(finger2);
                if (next == null || next == finger2) {
                    return finger2;
                }
                finger2 = next;
                idx2 = rpoIndex.get(finger2);
                if (idx2 == null) {
                    return finger1;
                }
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
        // This transformer instance is reused across methods, so all analysis caches
        // must be reset for each conversion.
        blocksByOffset.clear();
        successors.clear();
        predecessors.clear();
        idom.clear();
        dominanceTree.clear();
        visitedBlocks.clear();
        loopHeaders.clear();

        this.basicBlocks = basicBlocks;
        for (var block : basicBlocks) {
            blocksByOffset.put(block.offset(), block);
        }
        buildCfg();
        computeDominators();

        if (basicBlocks.isEmpty()) return new FlowBlock(new ArrayList<>());
        return reconstruct(basicBlocks.getFirst(), null);
    }

    private CodeTree negate(CodeTree condition) {
        if (condition instanceof CodeTree.Compare(
                ComparisonType comparison, CodeTree lhs, CodeTree rhs
        )) {
            var negated = switch (comparison) {
                case EQUAL -> ComparisonType.NOT_EQUAL;
                case NOT_EQUAL -> ComparisonType.EQUAL;
                case GREATER_THAN -> ComparisonType.LESS_THAN_OR_EQ;
                case LESS_THAN -> ComparisonType.GREATER_THAN_OR_EQ;
                case GREATER_THAN_OR_EQ -> ComparisonType.LESS_THAN;
                case LESS_THAN_OR_EQ -> ComparisonType.GREATER_THAN;
            };
            return new CodeTree.Compare(negated, lhs, rhs);
        }
        return new CodeTree.Negate(condition);
    }

    private FlowBlock reconstruct(BasicBlock block, BasicBlock stopAt) {
        if (block == null || block == stopAt || visitedBlocks.contains(block)) {
            return new FlowBlock(new ArrayList<>());
        }

        if (isLoopHeader(block) && !loopHeaders.contains(block)) {
            loopHeaders.add(block);
            BasicBlock exit = findLoopExit(block);

            // Detect while-like loop header
            if (block.statements().size() == 1 && block.terminator() instanceof Terminator.BranchIf(
                    CodeTree condition, int aTrue, int aFalse
            )) {
                var ifTrue = blocksByOffset.get(aTrue);
                var ifFalse = blocksByOffset.get(aFalse);

                if (ifTrue == exit || ifFalse == exit) {
                    var whileCondition = (ifTrue == exit) ? negate(condition) : condition;
                    var bodyStart = (ifTrue == exit) ? ifFalse : ifTrue;

                    FlowBlock body = reconstruct(bodyStart, block);

                    List<CodeTree> statements = new ArrayList<>();
                    statements.add(new CodeTree.ExecuteFlow(new ReconstructedFlow.While(whileCondition, body)));
                    if (exit != null && exit != stopAt) {
                        statements.addAll(reconstruct(exit, stopAt).statements());
                    }
                    return new FlowBlock(statements);
                }
            }

            FlowBlock body = reconstructLoopBody(block, exit);

            List<CodeTree> statements = new ArrayList<>();
            statements.add(new CodeTree.ExecuteFlow(new ReconstructedFlow.LoopForever(body)));

            if (exit != null) {
                statements.addAll(reconstruct(exit, stopAt).statements());
            }
            return new FlowBlock(statements);
        }

        visitedBlocks.add(block);
        List<CodeTree> statements = new ArrayList<>();

        var blockStatements = block.statements();

        var branchChain = trailingBranchChain(blockStatements);
        if (branchChain != null) {
            for (int i = 0; i < branchChain.startIndex(); i++) {
                statements.add(blockStatements.get(i));
            }

            BasicBlock merge = findMergePoint(block);
            Function<BasicBlock, FlowBlock> targetResolver = target -> reconstructTarget(target, merge);

            statements.addAll(reconstructConditionalChain(branchChain.branches(), 0, targetResolver).statements());

            if (merge != null) {
                statements.addAll(reconstruct(merge, stopAt).statements());
            }
            return new FlowBlock(statements);
        }

        for (int i = 0; i < blockStatements.size() - 1; i++) {
            statements.add(blockStatements.get(i));
        }

        var term = block.terminator();
        if (term instanceof Terminator.Jump(int target1)) {
            var target = blocksByOffset.get(target1);
            if (target != null && !isBackEdge(block, target) && target != stopAt) {
                statements.addAll(reconstruct(target, stopAt).statements());
            }
        } else if (term instanceof Terminator.BranchIf(CodeTree operand, int aTrue, int aFalse)) {
            var ifTrue = blocksByOffset.get(aTrue);
            var ifFalse = blocksByOffset.get(aFalse);

            BasicBlock merge = findMergePoint(block);

            statements.add(new CodeTree.ExecuteFlow(new ReconstructedFlow.If(
                    operand,
                    reconstruct(ifTrue, merge),
                    Optional.of(reconstruct(ifFalse, merge))
            )));

            if (merge != null) {
                statements.addAll(reconstruct(merge, stopAt).statements());
            }
        } else if (term instanceof Terminator.ReturnValue || term instanceof Terminator.ReturnVoid || term instanceof Terminator.Unreachable) {
            statements.add(term);
        }

        return new FlowBlock(statements);
    }

    private FlowBlock reconstructTarget(BasicBlock target, BasicBlock stopAt) {
        if (target == null || target == stopAt) {
            return new FlowBlock(new ArrayList<>());
        }
        return reconstruct(target, stopAt);
    }

    private BranchChain trailingBranchChain(List<CodeTree> statements) {
        int startIndex = statements.size();
        while (startIndex > 0 && statements.get(startIndex - 1) instanceof Terminator.BranchIf) {
            startIndex--;
        }

        if (startIndex == statements.size()) {
            return null;
        }

        List<Terminator.BranchIf> branches = new ArrayList<>();
        for (int i = startIndex; i < statements.size(); i++) {
            branches.add((Terminator.BranchIf) statements.get(i));
        }
        return new BranchChain(startIndex, branches);
    }

    private FlowBlock reconstructConditionalChain(
            List<Terminator.BranchIf> branches,
            int index,
            Function<BasicBlock, FlowBlock> targetResolver
    ) {
        var branch = branches.get(index);
        var ifTrue = targetResolver.apply(blocksByOffset.get(branch.ifTrue()));
        FlowBlock ifFalse = (index + 1 < branches.size())
                ? reconstructConditionalChain(branches, index + 1, targetResolver)
                : targetResolver.apply(blocksByOffset.get(branch.ifFalse()));

        return new FlowBlock(List.of(new CodeTree.ExecuteFlow(new ReconstructedFlow.If(
                branch.operand(),
                ifTrue,
                Optional.of(ifFalse)
        ))));
    }

    private FlowBlock reconstructLoopBody(BasicBlock header, BasicBlock exit) {
        // Special reconstruction for loop body that allows entering the header
        List<CodeTree> statements = new ArrayList<>();
        var blockStatements = header.statements();

        var branchChain = trailingBranchChain(blockStatements);
        if (branchChain != null) {
            for (int i = 0; i < branchChain.startIndex(); i++) {
                statements.add(blockStatements.get(i));
            }

            BasicBlock merge = findMergePoint(header);
            if (merge == exit) merge = null;

            BasicBlock branchStop = merge == null ? exit : merge;
            Function<BasicBlock, FlowBlock> targetResolver = target -> {
                if (target == header) {
                    return new FlowBlock(List.of(new Terminator.Continue()));
                }
                if (target == exit) {
                    return new FlowBlock(List.of(new Terminator.Break()));
                }
                return reconstructTarget(target, branchStop);
            };

            statements.addAll(reconstructConditionalChain(branchChain.branches(), 0, targetResolver).statements());

            if (merge != null) {
                statements.addAll(reconstruct(merge, exit).statements());
            }
            return new FlowBlock(statements);
        }

        for (int i = 0; i < blockStatements.size() - 1; i++) {
            statements.add(blockStatements.get(i));
        }

        var term = header.terminator();
        if (term instanceof Terminator.Jump(int target1)) {
            var target = blocksByOffset.get(target1);
            if (target != null && target != header && target != exit) {
                statements.addAll(reconstruct(target, exit).statements());
            } else if (target == header) {
                statements.add(new Terminator.Continue());
            } else if (target == exit) {
                statements.add(new Terminator.Break());
            }
        } else if (term instanceof Terminator.BranchIf(CodeTree operand, int aTrue, int aFalse)) {
            var ifTrue = blocksByOffset.get(aTrue);
            var ifFalse = blocksByOffset.get(aFalse);

            BasicBlock merge = findMergePoint(header);
            if (merge == exit) merge = null;

            statements.add(new CodeTree.ExecuteFlow(new ReconstructedFlow.If(
                    operand,
                    (ifTrue == header) ? new FlowBlock(List.of(new Terminator.Continue())) : (ifTrue == exit ? new FlowBlock(List.of(new Terminator.Break())) : reconstruct(ifTrue, merge == null ? exit : merge)),
                    Optional.of((ifFalse == header) ? new FlowBlock(List.of(new Terminator.Continue())) : (ifFalse == exit ? new FlowBlock(List.of(new Terminator.Break())) : reconstruct(ifFalse, merge == null ? exit : merge)))
            )));

            if (merge != null) {
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
        while (true) {
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
