package diamondfire.internal;

public class VarItemGen {
    public static native Object tag(String tag, String option);

    public static native Object gameValue(String value, String target);

    public static native Object lineVar();

    public static native Object vanillaItem(String nbt);
}
