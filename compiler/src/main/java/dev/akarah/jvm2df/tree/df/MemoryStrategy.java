package dev.akarah.jvm2df.tree.df;

import dev.akarah.jvm2df.tree.instructions.CodeTree;

public interface MemoryStrategy {
    String allocate();
    void increaseReference(String allocation);
    void decreaseReference(String allocation);
}
