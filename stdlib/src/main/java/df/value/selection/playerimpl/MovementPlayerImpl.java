package df.value.selection.playerimpl;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.value.Location;
import df.value.selection.generic.Movement;

public interface MovementPlayerImpl extends Movement {
    @Override
    default void teleport(Location location) {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction(
                "Teleport",
                location,
                VarItemGen.tag("Keep Velocity", "False"),
                VarItemGen.tag("Keep Current Rotation", "False")
        );
        CodeBlocks.selectObject("Reset");
    }
}
