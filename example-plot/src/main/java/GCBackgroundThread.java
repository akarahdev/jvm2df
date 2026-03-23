import diamondfire.Control;

public class GCBackgroundThread extends Thread {
    private Runtime RUNTIME;

    static {
        new GCBackgroundThread().start();
    }

    @Override
    public void run() {
        RUNTIME = Runtime.getRuntime();
        while (true) {
            if (RUNTIME.freeMemory() < 4500) {
                RUNTIME.gc();
            }
            Control.wait(1);
            Control.debug(RUNTIME.freeMemory());
        }
    }
}
