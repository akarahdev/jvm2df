import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var obj = ClassThree.generate(); //always ClassThree
        var cast1 = (ClassOne) obj; // must succeed
        var cast2 = (ClassTwo) obj; // will always fail
    }
}