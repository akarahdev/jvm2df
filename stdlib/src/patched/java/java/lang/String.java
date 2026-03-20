package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public class String {
    public String concat(String other) {
        return CodeBlocks.setVar(
                "+",
                VarItemGen.lineVar(),
                this,
                other
        );
    }

    public int length() {
        return CodeBlocks.setVar(
                "StringLength",
                VarItemGen.lineVar(),
                this
        );
    }

    public char charAt(int index) {
        return CodeBlocks.setVar(
                "TrimString",
                VarItemGen.lineVar(),
                this,
                index - 1,
                index - 1
        );
    }
}
