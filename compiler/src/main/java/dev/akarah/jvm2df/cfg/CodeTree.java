package dev.akarah.jvm2df.cfg;

import java.lang.classfile.CodeElement;
import java.lang.constant.ConstantDesc;
import java.util.List;

public interface CodeTree {
    record Constant(ConstantDesc constantDesc) implements CodeTree {}
    record StoreLocal(int idx, CodeTree value) implements CodeTree {}
    record IncrementLocal(int idx, CodeTree value) implements CodeTree {}
    record LoadLocal(int idx) implements CodeTree {}
    record Invoke(String descriptor, List<CodeTree> args) implements CodeTree {}

    record Add(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Sub(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Mul(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Div(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Mod(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Negate(CodeTree lhs) implements CodeTree {}
    record ShiftLeft(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record ShiftRight(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record And(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Or(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Xor(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record CompareNumbers(CodeTree lhs, CodeTree rhs) implements CodeTree {}

    record IsEqual(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record IsNotEqual(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record IsGE(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record IsGT(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record IsLE(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record IsLT(CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Unknown(CodeElement codeElement) implements CodeTree {}
}
