package diamondfire.value.plot;

import diamondfire.internal.CodeBlocks;
import diamondfire.value.ItemStack;
import diamondfire.value.Location;
import diamondfire.value.selection.EntitySelection;

public interface EntitySpawningImpl {
    default EntitySelection spawnMob(String mobType, Location location) {
        CodeBlocks.gameAction(
                "SpawnMob",
                new ItemStack(mobType + "_spawn_egg").raw(),
                location
        );
        return EntitySelection.lastSpawnedEntity();
    }
}
