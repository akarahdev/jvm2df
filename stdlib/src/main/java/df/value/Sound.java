package df.value;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;

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

    public Object raw() {
        return this.inner;
    }
}
