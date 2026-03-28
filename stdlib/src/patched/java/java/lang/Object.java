package java.lang;

import diamondfire.internal.VarItemGen;

public class Object {
    public boolean equals(Object other) {
        return this == other;
    }

    public String toString() {
        return VarItemGen.classOf(this);
    }

    public Class<?> getClass() {
        return new Class((String) VarItemGen.classOf(this));
    }
}
