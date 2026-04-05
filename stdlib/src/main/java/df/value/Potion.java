package df.value;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;

public final class Potion {
    Object inner;

    public Potion(String id) {
        this.inner = CodeBlocks.setVarR(
                "SetPotionType",
                VarItemGen.lineVar(),
                VarItemGen.emptyPotion(),
                id
        );
    }

    public Object raw() {
        return this.inner;
    }
}
