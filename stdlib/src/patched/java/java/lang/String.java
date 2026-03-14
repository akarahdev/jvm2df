package java.lang;

import diamondfire.internal.BlockTagGen;
import diamondfire.internal.CodeBlocks;

public class String {
    public String concat(String other) {
        return CodeBlocks.setVar(
                "+",
                BlockTagGen.lineVar(),
                this,
                other
        );
    }

    public int length() {
        return CodeBlocks.setVar(
                "StringLength",
                BlockTagGen.lineVar(),
                this
        );
    }

    public char charAt(int index) {
        return CodeBlocks.setVar(
                "TrimString",
                BlockTagGen.lineVar(),
                this,
                index - 1,
                index - 1
        );
    }
}
