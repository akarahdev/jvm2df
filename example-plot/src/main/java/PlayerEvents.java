import diamondfire.Control;
import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {

    }

    @Override
    public void Sneak() {
        for (int i = 0; i < 4000; i++) {
            var a = new Object();
        }
        Control.wait(40);
        System.gc();
    }
}
