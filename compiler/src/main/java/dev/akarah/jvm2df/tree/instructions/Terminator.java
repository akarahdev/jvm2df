package dev.akarah.jvm2df.tree.instructions;

public sealed interface Terminator extends CodeTree {
    record Jump(int target) implements Terminator {
    }

    record ReturnVoid() implements Terminator {
    }

    record ReturnValue(CodeTree code) implements Terminator {
    }

    record BranchIf(CodeTree operand, int ifTrue, int ifFalse) implements Terminator {
    }

    record Unreachable() implements Terminator {
    }
}
