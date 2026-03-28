package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public final class Particle {
    Object inner;

    public Particle(String id) {
        this.inner = CodeBlocks.setVarR(
                "SetParticleType",
                VarItemGen.lineVar(),
                VarItemGen.emptyParticle(),
                id
        );
    }

    public Particle setType(String id) {
        this.inner = CodeBlocks.setVarR(
                "SetParticleType",
                VarItemGen.lineVar(),
                VarItemGen.emptyParticle(),
                id
        );
        return this;
    }

    public Particle setAmount(int value) {
        this.inner = CodeBlocks.setVarR(
                "SetParticleAmount",
                VarItemGen.lineVar(),
                value
        );
        return this;
    }

    public Particle setSpread(double horizontal, double vertical) {
        this.inner = CodeBlocks.setVarR(
                "SetParticleSpread",
                VarItemGen.lineVar(),
                horizontal,
                vertical
        );
        return this;
    }
}
