package dev.akarah.jvm2df.tree.instructions;

import dev.akarah.jvm2df.tree.cfr.ReconstructedFlow;

import java.lang.classfile.CodeElement;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

/**
 * A tree-based code representation based on JVM bytecode.
 * This is a more abstract representation over JVM bytecode, making
 * virtual operations explicitly virtual, and static operations simpler.
 * This can be thought of as a hybrid of DiamondFire code blocks and
 * JVM bytecode.
 */
public interface CodeTree {
    record Constant(ConstantDesc constantDesc) implements CodeTree {}
    record StoreLocal(int idx, CodeTree value) implements CodeTree {}
    record IncrementLocal(int idx, CodeTree value) implements CodeTree {}
    record LoadLocal(int idx) implements CodeTree {}
    record Invoke(MemberRefEntry descriptor, List<CodeTree> args, InvokeStyle style) implements CodeTree {
        public MethodTypeDesc methodTypeDesc() {
            return MethodTypeDesc.ofDescriptor(this.descriptor().type().stringValue());
        }
    }

    record BinOp(BinOpType type, CodeTree lhs, CodeTree rhs) implements CodeTree {}
    record Negate(CodeTree lhs) implements CodeTree {}

    record ArrayNew(CodeTree size) implements CodeTree {}
    record ArrayIndex(CodeTree list, CodeTree index) implements CodeTree {}
    record ArrayStore(CodeTree list, CodeTree index, CodeTree value) implements CodeTree {}
    record ArrayLength(CodeTree list) implements CodeTree {}

    record ObjectNew(String clazz) implements CodeTree {}
    record ObjectGetField(CodeTree obj, String field) implements CodeTree {}
    record ObjectSetField(CodeTree obj, String field, CodeTree value) implements CodeTree {}
    record ObjectGetStatic(String clazz, String field) implements CodeTree {}
    record ObjectSetStatic(String clazz, String field, CodeTree value) implements CodeTree {}

    record Compare(ComparisonType comparison, CodeTree lhs, CodeTree rhs) implements CodeTree {}

    record ExecuteFlow(ReconstructedFlow flow) implements CodeTree {}
    record Unknown(CodeElement codeElement) implements CodeTree {}
}
