package java.lang;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;

public final class Float {
    public static String toString(float i) {
        return CodeBlocks.setVarR(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Float valueOf(float i);

    public native float floatValue();
}
