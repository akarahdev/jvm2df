package diamondfire.value.selection.generic;

import diamondfire.value.Location;
import diamondfire.value.selection.Selection;

public interface Movement extends Selection {
    void teleport(Location location);
}
