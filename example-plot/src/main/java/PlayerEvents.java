import df.event.PlayerEventHandler;
import df.value.Potion;
import df.value.selection.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    @SuppressWarnings("all")
    public void Join() {
        var sel = PlayerSelection.defaultTarget();
        var h = new Potion("Speed");
    }
}