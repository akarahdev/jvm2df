import diamondfire.Control;

public class GCBackgroundThread extends Thread {
    static {
        new GCBackgroundThread().start();
    }

    @Override
    public void run() {
        while (true) {
            var rt = Runtime.getRuntime();
            if (rt.freeMemory() < 4500) {
                rt.gc();
            }
            Control.wait(1);
            Control.debug(rt.freeMemory());
        }
    }
}
