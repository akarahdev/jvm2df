package dev.akarah.jvm2df.tree.df;

import dev.akarah.jvm2df.codetemplate.items.LiteralItem;
import dev.akarah.jvm2df.codetemplate.items.VariableItem;
import dev.akarah.jvm2df.tree.CompilationGraph;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.MethodTypeDesc;

public class VarPattern {
    public static VariableItem local(int idx) {
        return new VariableItem("local::" + idx, "line");
    }

    public static VariableItem namespaced(String namespace) {
        return new VariableItem(namespace + "::" + new Object().hashCode(), "unsaved");
    }

    public static VariableItem temporary(String namespace) {
        return new VariableItem("tmp::" + namespace + "::" + new Object().hashCode(), "line");
    }

    public static VariableItem classInfo(ClassEntry classEntry) {
        return classInfo(classEntry.asInternalName());
    }

    public static VariableItem classInfo(String internalName) {
        return new VariableItem("class::" + internalName, "unsaved");
    }

    public static String methodInfo(CompilationGraph.MethodOutline outline) {
        return methodInfo(outline.name(), outline.typeDesc());
    }

    public static String methodInfo(String name, MethodTypeDesc typeDesc) {
        return "method::" + name + typeDesc.descriptorString();
    }

    public static LiteralItem newMemoryAddress() {
        return LiteralItem.string("heap::%round(%random(1,10000000))");
    }
}
