package dev.akarah.jvm2df.tree.cfr;

import dev.akarah.jvm2df.tree.instructions.CodeTree;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public record FlowBlock(
        List<CodeTree> statements
) {
    public static FlowBlock by(CodeTree... statement) {
        return new FlowBlock(Arrays.asList(statement));
    }

    @Override
    public @NotNull String toString() {
        var sb = new StringBuilder();
        sb.append("block").append("{\n");
        for (var stmt : statements) {
            sb.append(stmt.toString()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
