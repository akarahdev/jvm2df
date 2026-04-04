package df.value.selection.entityimpl;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.value.Location;
import df.value.selection.generic.Movement;

public interface MovementEntityImpl extends Movement {
    @Override
    default void teleport(Location location) {
        CodeBlocks.selectObject("EntityUUID", VarItemGen.readField(this, "inner"));
        CodeBlocks.entityAction(
                "Teleport",
                location,
                VarItemGen.tag("Keep Current Rotation", "False")
        );
        CodeBlocks.selectObject("Reset");
    }
}
