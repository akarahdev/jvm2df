import diamondfire.event.PlayerEventHandler;
import diamondfire.value.plot.Plot;
import diamondfire.value.selection.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var sel = PlayerSelection.defaultTarget();
        for (int i = 0; i < 10; i++) {
            Plot.get().spawnMob("zombie", sel.location());
        }
    }
}