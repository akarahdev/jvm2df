import diamondfire.Control;

public class PlayerLoopThread extends Thread {
    @Override
    public void run() {
        int i = 0;
        while (true) {
            i += 1;
            Control.debug(i);
            Control.wait(1);
        }
    }
}
