package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.ShimException;
import diamondfire.internal.VarItemGen;

public class Integer {
    public static String toString(int i) {
        return CodeBlocks.setVar(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Integer valueOf(int i);
    public native int intValue();
}
