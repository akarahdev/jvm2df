package diamondfire.internal;

public class CodeBlocks {
    public static native void control(
            String action
    );

    public static native void control(
            String action,
            Object a,
            Object b
    );

    public static native void control(
            String action,
            Object a,
            Object b,
            Object c,
            Object d,
            Object e,
            Object f
    );

    public static native void playerAction(
            String action,
            Object a,
            Object b,
            Object c,
            Object d
    );

    public static native <T> T setVar(
            String action,
            Object a
    );

    public static native <T> T setVar(
            String action,
            Object a,
            Object b
    );

    public static native <T> T setVar(
            String action,
            Object a,
            Object b,
            Object c
    );

    public static native <T> T setVar(
            String action,
            Object a,
            Object b,
            Object c,
            Object d
    );

    public static native <T> T selectObject(
            String action
    );

    public static native <T> T selectObject(
            String action,
            Object a
    );
}
