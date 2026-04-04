package df.value.plot;

import df.internal.CodeBlocks;
import df.value.Location;

public interface BlockManipulation {
    default void setBlock(String material, Location location) {
        CodeBlocks.gameAction(
                "SetBlock",
                material,
                location
        );
    }
}
