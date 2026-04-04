package df.value.selection.generic;

import df.value.Location;
import df.value.selection.Selection;

public interface Movement extends Selection {
    void teleport(Location location);
}
