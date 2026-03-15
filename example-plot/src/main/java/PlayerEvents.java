import diamondfire.PlayerAction;
import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var a = new ComplexNumber(10, 20);
        var b = new ComplexNumber(30, 40);
        PlayerAction.sendMessage(a.toString());
        PlayerAction.sendMessage(b.toString());
        PlayerAction.sendMessage(a.add(b).toString());
        PlayerAction.sendMessage(a.multiply(b).toString());
    }
}
