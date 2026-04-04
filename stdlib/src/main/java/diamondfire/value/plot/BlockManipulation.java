package diamondfire.value.plot;

import diamondfire.internal.CodeBlocks;
import diamondfire.value.Location;

public interface BlockManipulation {
    default void setBlock(String material, Location location) {
        CodeBlocks.gameAction(
                "SetBlock",
                material,
                location
        );
    }
}
