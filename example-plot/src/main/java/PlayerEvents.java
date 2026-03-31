import diamondfire.event.PlayerEventHandler;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var sel = PlayerSelection.defaultTarget();
        sel.sendMessage(Text.of("<gray>Hello <blue>%default<gray>!"));
    }
}