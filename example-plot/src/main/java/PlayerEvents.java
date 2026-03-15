import diamondfire.Selection;
import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var sel = Selection.defaultTarget();
        sel.sendMessage("Welcome to the game!");
        sel.sendMessage("Have you considered meowing?");
        for (int i = 0; i < 10; i++) {
            sel.sendMessage(Integer.toString(i));
        }
    }
}
