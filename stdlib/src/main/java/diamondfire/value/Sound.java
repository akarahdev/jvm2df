package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public final class Sound {
    public static Sound of(String id) {
        return CodeBlocks.setVar(
                "SetSoundType",
                VarItemGen.lineVar(),
                VarItemGen.emptySound(),
                id
        );
    }
}
