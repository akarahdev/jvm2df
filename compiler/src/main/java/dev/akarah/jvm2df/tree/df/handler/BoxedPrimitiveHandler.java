package dev.akarah.jvm2df.tree.df.handler;

import dev.akarah.jvm2df.codetemplate.items.VarItem;
import dev.akarah.jvm2df.tree.df.CodeBlockTransformer;
import dev.akarah.jvm2df.tree.instructions.CodeTree;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class BoxedPrimitiveHandler implements InvokeHandler {
    public static Set<String> CLASS_NAMES = Set.of(
            "java/lang/Integer",
            "java/lang/Long",
            "java/lang/Double",
            "java/lang/Float",
            "java/lang/Character",
            "java/lang/Boolean",
            "java/lang/Short",
            "java/lang/Byte"
    );

    public static Set<String> METHOD_NAMES = Set.of(
            "valueOf",
            "intValue",
            "longValue",
            "floatValue",
            "doubleValue",
            "charValue",
            "byteValue",
            "shortValue",
            "booleanValue"
    );

    @Override
    public Optional<Function<CodeBlockTransformer, VarItem<?>>> tryRewrite(CodeTree.Invoke invoke) {
        for(var className : CLASS_NAMES) {
            for(var methodName : METHOD_NAMES) {
                if(invoke.descriptor().owner().asInternalName().equals(className)
                && invoke.descriptor().name().equalsString(methodName)) {
                    return Optional.of(
                            transformer -> transformer.convertCodeTree(invoke.args().getFirst())
                    );
                }
            }
        }

        return Optional.empty();
    }
}
