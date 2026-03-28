import diamondfire.event.PlayerEventHandler;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var selection = PlayerSelection.defaultTarget();
        var text = Text.of(Something.generateString());
        selection.sendMessage(text);
        selection.sendMessage(Text.of(Something.generateString().getClass().toString()));

        selection.sendMessage(Text.of((int) Something.generateInt()));
    }

    @Override
    public void Sneak() {
    }
}