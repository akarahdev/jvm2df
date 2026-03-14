package dev.akarah.jvm2df.cfg;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.util.*;

public class BytecodeTranslator {
    CodeModel codeModel;
    List<CodeElement> instructions;
    Set<Integer> splitTargets;
    CodeTreeConverter converter;

    public BytecodeTranslator(CodeModel codeModel) {
        this.instructions = codeModel.elementList();
        this.splitTargets = new HashSet<>();
        this.codeModel = codeModel;
    }

    public List<BasicBlock> split() {
        this.findTargets();
        return constructBlocks();
    }

    private List<BasicBlock> constructBlocks() {
        var list = new ArrayList<BasicBlock>();
        int offset = 0;
        var block = new BasicBlock(
                offset,
                new ArrayList<>()
        );
        this.converter = new CodeTreeConverter(block.statements(), this::labelToOffset);
        for(var elem : this.instructions) {
            if(elem instanceof Label label) {
                var targetOffset = labelToOffset(label);
                if (splitTargets.contains(targetOffset)) {
                    if (!block.statements().isEmpty() || list.isEmpty() || block.offset() != targetOffset) {
                        block = updateBasicBlock(list, offset, block, targetOffset);
                    }
                }
            }
            if(elem instanceof Instruction instruction) {
                this.converter.convert(elem, offset);
                offset += instruction.sizeInBytes();
                if(splitTargets.contains(offset)) {
                    block = updateBasicBlock(list, offset, block, offset);
                }
            }
        }
        if (!block.statements().isEmpty() || list.isEmpty()) {
            list.add(block);
        }
        if(list.getFirst().statements().isEmpty()) {
            list.removeFirst();
        }
        return list;
    }

    @NonNull
    private BasicBlock updateBasicBlock(ArrayList<BasicBlock> list, int offset, BasicBlock block, int targetOffset) {
        list.add(block);
        if(!block.statements().isEmpty() && !(block.statements().getLast() instanceof Terminator)) {
            block.statements().add(new Terminator.Jump(offset));
        }
        block = new BasicBlock(
                targetOffset,
                new ArrayList<>()
        );
        this.converter = new CodeTreeConverter(block.statements(), this::labelToOffset);
        return block;
    }

    private int labelToOffset(Label label) {
        return ((CodeAttribute) this.codeModel).labelToBci(label);
    }

    private void findTargets() {
        this.splitTargets.clear();
        int offset = 0;
        for(var element : this.instructions) {
            if(element instanceof Label label) {
                this.splitTargets.add(labelToOffset(label));
            }
            if(element instanceof Instruction instruction) {
                if(element instanceof ReturnInstruction
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
