import diamondfire.event.PlayerEventHandler;
import diamondfire.value.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        new EffectsThread(PlayerSelection.defaultTarget()).start();
    }

    @Override
    public void Sneak() {
    }
}
