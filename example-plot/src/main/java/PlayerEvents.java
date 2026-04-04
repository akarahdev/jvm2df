import diamondfire.event.PlayerEventHandler;
import diamondfire.value.Location;
import diamondfire.value.selection.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var sel = PlayerSelection.defaultTarget();
        sel.teleport(Location.of(Datas.x() + Datas.y(), 50, 10));
    }
}