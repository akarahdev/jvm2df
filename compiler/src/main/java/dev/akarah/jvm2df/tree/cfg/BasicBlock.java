package dev.akarah.jvm2df.tree.cfg;

import dev.akarah.jvm2df.tree.cfr.FlowBlock;
import dev.akarah.jvm2df.tree.instructions.CodeTree;
import dev.akarah.jvm2df.tree.instructions.Terminator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record BasicBlock(
        int offset,
        List<CodeTree> statements
) {
    public CodeTree terminator() {
        if (statements.isEmpty()) {
            return new Terminator.Unreachable();
        }
        return statements.getLast();
    }

    public FlowBlock mapToFlow(Function<Terminator, CodeTree> mapper) {
        var statements = new ArrayList<CodeTree>(this.statements);
        if (statements.getLast() instanceof Terminator) {
            statements.add(mapper.apply((Terminator) statements.removeLast()));
        }
        return new FlowBlock(statements);
    }

    @Override
    public @NotNull String toString() {
        var sb = new StringBuilder();
        sb.append("bb@").append(offset).append("{\n");
        for (var stmt : statements) {
            sb.append("  ").append(stmt.toString()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
