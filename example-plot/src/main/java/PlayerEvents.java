import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var obj = new ClassThree();
        var cast1 = (ClassOne) obj; // must succeed
        var cast2 = (ClassTwo) (Object) obj; // will always fail
    }
}