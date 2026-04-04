package java.lang;

import diamondfire.internal.VarItemGen;

public abstract class Enum<E extends Enum<E>> {
    protected Enum(String name, int ordinal) {
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> Enum<E> valueOf(Class<E> enumClass, String name) {
        return (Enum<E>) VarItemGen.readStaticField(
                VarItemGen.classHandle(enumClass.toString()),
                name
        );
    }
}
