package java.lang;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;

public final class Long {
    public static String toString(long i) {
        return CodeBlocks.setVarR(
                "String",
                VarItemGen.lineVar(),
                i,
                VarItemGen.tag("Text Value Merging", "No spaces")
        );
    }

    public static native Long valueOf(long i);

    public native long longValue();
}
