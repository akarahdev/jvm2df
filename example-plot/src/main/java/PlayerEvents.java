import diamondfire.Control;
import diamondfire.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var a1 = new ComplexNumber(10, 20);
        var a2 = new ComplexNumber(30, 40);
        a1.multiply(a2).conjugate().debugPrint();
    }
}
