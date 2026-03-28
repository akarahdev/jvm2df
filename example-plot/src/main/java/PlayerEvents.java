import diamondfire.event.PlayerEventHandler;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var selection = PlayerSelection.defaultTarget();
        var text = Text.of(Something.generate());
        selection.sendMessage(text);
        selection.sendMessage(Text.of(Something.generate().getClass().toString()));
    }

    @Override
    public void Sneak() {
    }
}