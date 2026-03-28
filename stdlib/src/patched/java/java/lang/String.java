package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public class String {
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
