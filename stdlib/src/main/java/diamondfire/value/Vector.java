package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public final class Vector {
    public static Vector of(double x, double y, double z) {
        return CodeBlocks.setVar(
                "Vector",
                VarItemGen.lineVar(),
                x,
                y,
                z
        );
    }

    public double x() {
        return CodeBlocks.setVar(
                "GetVectorComp",
                VarItemGen.lineVar(),
                this,
                VarItemGen.tag("Component", "X")
        );
    }

    public double y() {
        return CodeBlocks.setVar(
                "GetVectorComp",
                VarItemGen.lineVar(),
                this,
                VarItemGen.tag("Component", "Y")
        );
    }

    public double z() {
        return CodeBlocks.setVar(
                "GetVectorComp",
                VarItemGen.lineVar(),
                this,
                VarItemGen.tag("Component", "Z")
        );
    }
}
