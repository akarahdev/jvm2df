package java.lang;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;

public final class Integer {
    public static String toString(int i) {
        return CodeBlocks.setVarR(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Integer valueOf(int i);

    public native int intValue();
}
