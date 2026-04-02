package dev.akarah.jvm2df.tree;

import org.jetbrains.annotations.NotNull;

import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.*;

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
                model.thisClass().asSymbol().descriptorString(),
                model
        );
    }

    public Set<MethodOutline> allSuperMethodsFor(ClassModel inpm) {
        var list = new HashSet<MethodOutline>();
        for (var entry : this.allSuperClassesFor(inpm)) {
            if (this.classByEntry(entry) == null) {
                throw new NullPointerException("Entry class " + entry + " is not present in the JAR");
            }
            var model = this.classByEntry(entry);
            System.out.println("trying " + entry);
            for (var method : model.methods()) {
                if (method.flags().has(AccessFlag.STATIC)) {
                    continue;
                }
                list.add(new MethodOutline(
                        method.methodName().stringValue(),
                        method.methodTypeSymbol()
                ));
            }
        }
        return list;
    }

    public Set<ClassEntry> allSuperClassesFor(ClassModel model) {
        var list = new HashSet<ClassEntry>();

        // get super classes
        var entry = model.thisClass();
        do {
            list.add(entry);
            System.out.println("trying " + entry);
            if (this.classByEntry(entry) == null) {
                throw new NullPointerException("Entry class " + entry + " is not present in the JAR");
            }
            entry = this.classByEntry(entry).superclass().orElse(null);
        } while (entry != null);

        // get super interfaces
        var workList = new ArrayList<ClassEntry>();
        workList.add(model.thisClass());
        while (!workList.isEmpty()) {
            var currentEntry = workList.removeLast();
            if (this.classByEntry(currentEntry) == null) {
                throw new NullPointerException("Entry class " + entry + " is not present in the JAR");
            }
            var superClassesOfItf = this.classByEntry(currentEntry).interfaces();
            for (var sc : superClassesOfItf) {
                if (!list.contains(sc) && !workList.contains(sc)) {
                    workList.add(sc);
                    list.add(sc);
                }
            }
        }

        return list;
    }

    public Set<ClassEntry> allClassesExtending(ClassEntry classEntry) {
        var set = new HashSet<ClassEntry>();
        for (var classEntries : this.classDescs.entrySet()) {
            if (allSuperClassesFor(classEntries.getValue()).contains(classEntry)) {
                set.add(classEntries.getValue().thisClass());
            }
        }
        return set;
    }

    public ClassModel classByEntry(ClassEntry name) {
        if (name == null) {

            throw new NullPointerException("why null :c");
        }
        if (!this.classDescs.containsKey(name.asSymbol().descriptorString())) {
            throw new NullPointerException("Please compile with missing class " + name);
        }
        return this.classDescs.get(name.asSymbol().descriptorString());
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
        for (var superClass : allSuperClassesFor(classByEntry(baseClass))) {
            if (m != null) {
                break;
            }
            System.out.println("looking in " + superClass);
            m = lookupMethodExact(superClass, methodName, typeDesc);
        }
        if (m == null) {
            throw new RuntimeException("Could not find method " + methodName + typeDesc.descriptorString() + " in " + baseClass);
        }
        return m;
    }

    public String generateFunctionCallName(
            ClassEntry className,
            MethodOutline outline
    ) {
        return className.asSymbol().descriptorString() + "#" + outline;
    }

    public List<ClassModel> allClasses() {
        return this.classDescs.values().stream().toList();
    }
}
