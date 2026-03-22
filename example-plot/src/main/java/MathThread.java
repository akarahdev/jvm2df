import diamondfire.Control;

public class MathThread extends Thread {
    @Override
    public void run() {
        Control.debug("sin(5) = ");
        Control.debug(Math.sin(5));
    }
}
