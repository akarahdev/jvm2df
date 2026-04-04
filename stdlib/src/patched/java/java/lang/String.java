package java.lang;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.internal.annotation.NativeValue;

@NativeValue
public final class String {
    public String concat(String other) {
        return CodeBlocks.setVarR(
                "+",
                VarItemGen.lineVar(),
                this,
                other
        );
    }

    public int length() {
        return CodeBlocks.setVarI(
                "StringLength",
                VarItemGen.lineVar(),
                this
        );
    }

    public char charAt(int index) {
        return CodeBlocks.setVarC(
                "TrimString",
                VarItemGen.lineVar(),
                this,
                index - 1,
                index - 1
        );
    }
}
