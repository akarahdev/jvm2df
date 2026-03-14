package dev.akarah.jvm2df.tree.df.strategy;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;

public interface GlobalMemoryStrategy {
    VarItem<?> allocate();
    void setField(VarItem<?> allocation, VarItem<?> field, VarItem<?> value);
    void setStaticField(String clazz, String field, VarItem<?> value);
    VarItem<?> readField(VarItem<?> allocation, VarItem<?> field);
    VarItem<?> readStaticField(String clazz, String field);
    void reference(VarItem<?> allocation);
    void dereference(VarItem<?> allocation);
}
