package dev.akarah.jvm2df.tree.instructions;

import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;

import java.lang.classfile.CodeElement;
import java.lang.constant.ConstantDesc;
import java.util.List;

public interface CodeTree {
    record Constant(ConstantDesc constantDesc) implements CodeTree {}
    record StoreLocal(int idx, CodeTree value) implements CodeTree {}
    record IncrementLocal(int idx, CodeTree value) implements CodeTree {}
    record LoadLocal(int idx) implements CodeTree {}
    record Invoke(String descriptor, List<CodeTree> args) implements CodeTree {}

    record BinOp(BinOpType type, CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Negate(CodeTree lhs) implements CodeTree {}

    record ArrayNew(CodeTree size) implements CodeTree {}
    record ArrayIndex(CodeTree list, CodeTree index) implements CodeTree {}
    record ArrayStore(CodeTree list, CodeTree index, CodeTree value) implements CodeTree {}
    record ArrayLength(CodeTree list) implements CodeTree {}

    record Compare(ComparisonType comparison, CodeTree lhs, CodeTree rhs) implements CodeTree {}

    record ExecuteFlow(ReconstructedFlow flow) implements CodeTree {}
    record Unknown(CodeElement codeElement) implements CodeTree {}
}
