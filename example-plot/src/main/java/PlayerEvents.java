import diamondfire.Control;
import diamondfire.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        DemoClass.sumTheArrays();
    }

    @Override
    public void Sneak() {
        DemoClass.sumTheArrays();
    }
}
