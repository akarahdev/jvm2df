import diamondfire.event.PlayerEventHandler;
import diamondfire.value.Location;
import diamondfire.value.selection.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var sel = PlayerSelection.defaultTarget();
        sel.teleport(Location.of(10, 55, 10));
    }
}