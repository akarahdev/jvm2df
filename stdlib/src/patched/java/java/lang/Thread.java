package java.lang;

public abstract class Thread {
    private native void start0();

    public void start() {
        this.start0();
    }

    public abstract void run();
}
