import diamondfire.PlayerAction;
import diamondfire.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        PlayerAction.sendMessage("meow!".charAt(0));
        PlayerAction.sendMessage("Hello ".concat("world!"));
        PlayerAction.sendMessage("Hi!".length());
    }
}
