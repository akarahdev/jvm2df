import diamondfire.event.PlayerEventHandler;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var selection = PlayerSelection.defaultTarget();
        var text = Text.of("Hi!");
        selection.sendMessage(text);
    }

    @Override
    public void Sneak() {
    }
}