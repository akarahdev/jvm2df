package diamondfire.internal;

public class VarItemGen {
    public static native Object tag(String tag, String option);

    public static native Object tag(String tag, String option, Object defaultValue);

    public static native <T> T gameValue(String value, String target);

    public static native Object lineVar();

    public static native Object emptySound();

    public static native Object emptyParticle();

    public static native Object vanillaItem(String nbt);

    public static native Object classHandle(Object className);

    public static native Object readField(Object allocation, Object field);

    public static native Object readStaticField(Object classHandle, Object field);

    public static native String classOf(Object allocation);

    public static native void runGc();

    public static native long gcAllocationCount();

    public static native Object bucketVar(String namespace, String bucket, String name);
}
