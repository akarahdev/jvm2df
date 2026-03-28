package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class Integer {
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
