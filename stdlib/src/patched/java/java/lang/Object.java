package java.lang;

import diamondfire.internal.VarItemGen;

public class Object {
    public boolean equals(Object other) {
        return this == other;
    }

    public String toString() {
        return (String) VarItemGen.readField(this, "class");
    }

    public Class<?> getClass() {
        return Class.forName((String) VarItemGen.readField(this, "class"));
    }
}
