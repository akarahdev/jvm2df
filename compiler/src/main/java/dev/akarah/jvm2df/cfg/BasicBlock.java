package dev.akarah.jvm2df.cfg;

import java.util.List;

public record BasicBlock(
        int offset,
        List<CodeTree> statements
) {
    public CodeTree terminator() {
        if(statements.isEmpty()) {
            return new Terminator.Unreachable();
        }
        return statements.getLast();
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("bb@").append(offset).append("{\n");
        for(var stmt : statements) {
            sb.append("  ").append(stmt.toString()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
