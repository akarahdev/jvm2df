package java.lang;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;

public final class Double {
    public static String toString(double i) {
        return CodeBlocks.setVarR(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Double valueOf(double i);

    public native double doubleValue();
}
