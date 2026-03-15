package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.ShimException;
import diamondfire.internal.VarItemGen;

public class Float {
    public static String toString(float i) {
        return CodeBlocks.setVar(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Float valueOf(float i);
    public native float floatValue();
}
