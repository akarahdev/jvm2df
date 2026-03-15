import diamondfire.Control;
import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        Animal a = new Cat();
        a.makeSound();
        Control.debug(a.equals(a));
    }
}
