package diamondfire.value.selection.playerimpl;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.value.Location;
import diamondfire.value.selection.generic.Movement;

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
