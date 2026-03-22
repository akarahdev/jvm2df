import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        new MathThread().start();
    }

    @Override
    public void Sneak() {
        new PlayerLoopThread().start();
    }
}
