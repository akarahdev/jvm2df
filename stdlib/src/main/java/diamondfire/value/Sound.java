package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public final class Sound {
    Object inner;

    public Sound(String id) {
        this.inner = CodeBlocks.setVarR(
                "SetCustomSound",
                VarItemGen.lineVar(),
                VarItemGen.emptySound(),
                id
        );
    }
}
