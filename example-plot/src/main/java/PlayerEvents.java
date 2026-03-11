import diamondfire.Control;
import diamondfire.PlayerEventHandler;

public class PlayerEvents implements PlayerEventHandler {
    @Override
    public void playerJoin() {
        Control.log("Hello world!");
    }
}
