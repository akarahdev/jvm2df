package dev.akarah.jvm2df.tree.df.strategy;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;

public interface GlobalMemoryStrategy {
    VarItem<?> allocate();
    void setField(VarItem<?> allocation, String field, VarItem<?> value);
    VarItem<?> readField(VarItem<?> allocation, String field);
    void reference(VarItem<?> allocation);
    void dereference(VarItem<?> allocation);
}
