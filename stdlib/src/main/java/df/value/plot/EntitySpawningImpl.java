package df.value.plot;

import df.internal.CodeBlocks;
import df.value.ItemStack;
import df.value.Location;
import df.value.selection.EntitySelection;

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
