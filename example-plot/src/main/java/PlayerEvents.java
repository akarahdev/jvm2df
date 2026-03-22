import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {

    }

    @Override
    public void Sneak() {
        new PlayerLoopThread().start();
    }
}
