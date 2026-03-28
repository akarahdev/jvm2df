package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public final class Location {
    public static Location of(double x, double y, double z) {
        return CodeBlocks.setVarR("SetAllCoords", VarItemGen.lineVar(), x, y, z, VarItemGen.tag("Coordinate Type", "Plot coordinate"));
    }

    public static Location of(double x, double y, double z, double pitch, double yaw) {
        return CodeBlocks.setVarR("SetAllCoords", VarItemGen.lineVar(), x, y, z, pitch, yaw, VarItemGen.tag("Coordinate Type", "Plot coordinate"));
    }

    public double x() {
        return CodeBlocks.setVarD("GetCoord", VarItemGen.lineVar(), this, VarItemGen.tag("Coordinate Type", "Plot coordinate"), VarItemGen.tag("Coordinate", "X"));
    }

    public double y() {
        return CodeBlocks.setVarD("GetCoord", VarItemGen.lineVar(), this, VarItemGen.tag("Coordinate Type", "Plot coordinate"), VarItemGen.tag("Coordinate", "Y"));
    }

    public double z() {
        return CodeBlocks.setVarD("GetCoord", VarItemGen.lineVar(), this, VarItemGen.tag("Coordinate Type", "Plot coordinate"), VarItemGen.tag("Coordinate", "Z"));
    }
}
