package dev.akarah.jvm2df.cfg;

import java.lang.classfile.CodeElement;
import java.lang.classfile.Opcode;
import java.util.List;

public interface Terminator extends CodeTree {
    record Jump(int target) implements Terminator {}
    record ReturnVoid() implements Terminator {}
    record ReturnValue(CodeTree code) implements Terminator {}
    record BranchIf(CodeTree operand, int ifTrue, int ifFalse) implements Terminator {}
    record Unreachable() implements Terminator {}
}
