package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class Double {
    public static String toString(double i) {
        return CodeBlocks.setVar(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Double valueOf(double i);
    public native double doubleValue();
}
