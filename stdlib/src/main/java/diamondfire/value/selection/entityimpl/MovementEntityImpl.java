package diamondfire.value.selection.entityimpl;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.value.Location;
import diamondfire.value.selection.generic.Movement;

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
