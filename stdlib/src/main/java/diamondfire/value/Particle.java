package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public final class Particle {
    public static Particle of(String id) {
        return CodeBlocks.setVar(
                "SetParticleType",
                VarItemGen.lineVar(),
                VarItemGen.emptyParticle(),
                id
        );
    }
}
