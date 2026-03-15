package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class Long {
    public static String toString(long i) {
        return CodeBlocks.setVar(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Long valueOf(long i);
    public native long longValue();
}
