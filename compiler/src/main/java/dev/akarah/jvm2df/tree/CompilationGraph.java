package dev.akarah.jvm2df.tree;

import org.jetbrains.annotations.NotNull;

import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilationGraph {
    Map<String, ClassModel> classDescs = new HashMap<>();

    public record MethodOutline(
            String name,
            MethodTypeDesc typeDesc
    ) {
        @Override
        public @NotNull String toString() {
            return this.name() + this.typeDesc().descriptorString();
        }
    }

    public void register(ClassModel model) {
        this.classDescs.put(
                model.thisClass().asInternalName(),
                model
        );
    }

    public List<MethodOutline> allSuperMethodsFor(ClassModel model) {
        var list = new ArrayList<MethodOutline>();
        var entry = model.thisClass();
        do {
            for(var method : model.methods()) {
                for(var alreadyInList : list) {
                    if(alreadyInList.name().equals(method.methodName().stringValue())
                    && alreadyInList.typeDesc().equals(method.methodTypeSymbol())) {
                        continue;
                    }
                }
                list.add(new MethodOutline(
                        method.methodName().stringValue(),
                        method.methodTypeSymbol()
                ));
            }
            if(this.classByEntry(entry) == null) {
                throw new NullPointerException("Entry class " + entry + " is not present in the JAR");
            }
            entry = this.classByEntry(entry).superclass().orElse(null);
        } while(entry != null);
        return list;
    }

    public ClassModel classByEntry(ClassEntry name) {
        return this.classDescs.get(name.asInternalName());
    }

    public MethodModel lookupMethodExact(ClassEntry className, String methodName, MethodTypeDesc typeDesc) {
        return this.classByEntry(className).methods()
                .stream()
                .filter(x -> x.methodName().equalsString(methodName))
                .filter(x -> x.methodTypeSymbol().equals(typeDesc))
                .findFirst().orElse(null);
    }

    public MethodModel lookupMethodInSuper(ClassEntry baseClass, String methodName, MethodTypeDesc typeDesc) {
        MethodModel m;
        m = lookupMethodExact(baseClass, methodName, typeDesc);
        while(m == null && baseClass != null) {
            baseClass = classByEntry(baseClass).superclass().orElse(null);
        }
        return m;
    }

    public String generateFunctionCallName(
            ClassEntry className,
            MethodOutline outline
    ) {
        return className.asInternalName() + "#" + outline;
    }
}
