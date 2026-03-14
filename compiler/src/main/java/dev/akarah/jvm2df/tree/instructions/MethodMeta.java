package dev.akarah.jvm2df.tree.instructions;

import org.jetbrains.annotations.NotNull;

import java.lang.constant.MethodTypeDesc;

public record MethodMeta(
        String className,
        String methodName,
        MethodTypeDesc methodTypeDesc,
        boolean isStatic,
        String superClassName
) {
    @Override
    public @NotNull String toString() {
        return className + "#" + methodName + methodTypeDesc.descriptorString();
    }
}
