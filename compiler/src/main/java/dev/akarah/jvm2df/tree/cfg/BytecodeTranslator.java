package dev.akarah.jvm2df.tree.cfg;

import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.Terminator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.util.*;

/**
 * Transforms java bytecode into {@link BasicBlock} instances.
 * This works by finding certain points to split the bytecode at, and inserting
 * terminators between them as necessary to keep the control flow consistent.
 */
public class BytecodeTranslator {
    MethodModel methodModel;
    CodeModel codeModel;
    List<CodeElement> instructions;
    Set<Integer> splitTargets;
    CodeTreeConverter converter;
    CompilationGraph graph;

    public List<BasicBlock> split(MethodModel methodModel, CodeModel codeModel, CompilationGraph graph) {
        this.instructions = codeModel.elementList();
        this.splitTargets = new HashSet<>();
        this.codeModel = codeModel;
        this.graph = graph;
        this.findTargets();
        return constructBlocks();
    }

    /**
     * Assesses if an instruction can fall through to the next label.
     */
    private boolean canFallThrough(Instruction instruction) {
        Opcode op = instruction.opcode();
        return switch (op) {
            case GOTO, GOTO_W, IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN, ATHROW, LOOKUPSWITCH, TABLESWITCH ->
                    false;
            default -> true;
        };
    }

    /**
     * The primary method, responsible for constructing the series of basic blocks.
     * This takes our previous splits in the bytecode, and turns it into basic blocks,
     * converting with the {@link CodeTreeConverter} as needed.
     */
    private List<BasicBlock> constructBlocks() {
        var list = new ArrayList<BasicBlock>();
        int offset = 0;
        var block = new BasicBlock(
                offset,
                new ArrayList<>()
        );
        this.converter = new CodeTreeConverter(block.statements(), this::labelToOffset, graph);

        var labelStackSizes = new HashMap<Integer, Integer>();


        for (var elem : this.instructions) {
            if (elem instanceof Label label) {
                var targetOffset = labelToOffset(label);
                if (splitTargets.contains(targetOffset)) {
                    if (!block.statements().isEmpty() || list.isEmpty() || block.offset() != targetOffset) {
                        block = updateBasicBlock(list, offset, block, targetOffset, labelStackSizes);
                    }
                }
            }
            if (elem instanceof Instruction instruction) {
                this.converter.convert(elem, offset);
                switch (elem) {
                    case BranchInstruction branch -> {
                        labelStackSizes.put(labelToOffset(branch.target()), this.converter.stack.size());
                    }
                    case LookupSwitchInstruction sw -> {
                        int size = this.converter.stack.size();
                        labelStackSizes.put(labelToOffset(sw.defaultTarget()), size);
                        for (var entry : sw.cases()) {
                            labelStackSizes.put(labelToOffset(entry.target()), size);
                        }
                    }
                    case TableSwitchInstruction sw -> {
                        int size = this.converter.stack.size();
                        labelStackSizes.put(labelToOffset(sw.defaultTarget()), size);
                        for (var entry : sw.cases()) {
                            labelStackSizes.put(labelToOffset(entry.target()), size);
                        }
                    }
                    default -> {
                    }
                }

                offset += instruction.sizeInBytes();

                if (canFallThrough(instruction)) {
                    labelStackSizes.put(offset, this.converter.stack.size());
                }
                if (splitTargets.contains(offset)) {
                    block = updateBasicBlock(list, offset, block, offset, labelStackSizes);
                }
            }
        }
        if (!block.statements().isEmpty() || list.isEmpty()) {
            list.add(block);
        }
        if (list.getFirst().statements().isEmpty()) {
            list.removeFirst();
        }
        return list;
    }

    /**
     * Performs the actual splitting of a basic block into multiple,
     * carrying over stack elements, and converting into code trees as needed.
     */
    @NonNull
    private BasicBlock updateBasicBlock(
            ArrayList<BasicBlock> list,
            int offset,
            BasicBlock block,
            int targetOffset,
            Map<Integer, Integer> labelStackSizes
    ) {
        int stackSlotBase = ((CodeAttribute) this.codeModel).maxLocals() + 1;
        Terminator terminator = null;
        if (!block.statements().isEmpty() && block.statements().getLast() instanceof Terminator) {
            terminator = (Terminator) block.statements().removeLast();
        }

        for (int i = 0; i < this.converter.stack.size(); i++) {
            block.statements().add(new CodeTree.StoreLocal(stackSlotBase + i, this.converter.stack.get(i)));
        }

        if (terminator != null) {
            block.statements().add(terminator);
        }

        list.add(block);
        if (!block.statements().isEmpty() && !(block.statements().getLast() instanceof Terminator)) {
            block.statements().add(new Terminator.Jump(offset));

            labelStackSizes.put(offset, this.converter.stack.size());
        }
        block = new BasicBlock(
                targetOffset,
                new ArrayList<>()
        );

        this.converter = new CodeTreeConverter(block.statements(), this::labelToOffset, this.graph);

        int size = labelStackSizes.getOrDefault(targetOffset, 0);
        for (int i = 0; i < size; i++) {
            this.converter.stack.add(new CodeTree.LoadLocal(stackSlotBase + i));
        }

        return block;
    }

    private int labelToOffset(Label label) {
        return ((CodeAttribute) this.codeModel).labelToBci(label);
    }

    /**
     * Finds the locations in the bytecode to split instructions into basic blocks.
     */
    private void findTargets() {
        this.splitTargets.clear();
        int offset = 0;
        for (var element : this.instructions) {
            if (element instanceof Label label) {
                this.splitTargets.add(labelToOffset(label));
            }
            if (element instanceof Instruction instruction) {
                if (element instanceof ReturnInstruction
                        || element instanceof BranchInstruction) {
                    this.splitTargets.add(offset + instruction.sizeInBytes());
                    if (element instanceof BranchInstruction branch) {
                        this.splitTargets.add(labelToOffset(branch.target()));
                    }
                }
                if (element instanceof LookupSwitchInstruction sw) {
                    this.splitTargets.add(labelToOffset(sw.defaultTarget()));
                    for (var entry : sw.cases()) {
                        this.splitTargets.add(labelToOffset(entry.target()));
                    }
                }
                if (element instanceof TableSwitchInstruction sw) {
                    this.splitTargets.add(labelToOffset(sw.defaultTarget()));
                    for (var entry : sw.cases()) {
                        this.splitTargets.add(labelToOffset(entry.target()));
                    }
                }
                offset += instruction.sizeInBytes();
            }
        }
    }
}