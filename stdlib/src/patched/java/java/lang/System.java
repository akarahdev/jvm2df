package java.lang;

public final class System {
    public static void gc() {
        Runtime.getRuntime().gc();
    }

    @SuppressWarnings("all") // needed so ide doesn't shout about System#arrayCopy
    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        Object[] s = (Object[]) src;
        Object[] d = (Object[]) dest;
        for (int i = 0; i < length; i++) {
            d[destPos + i] = s[srcPos + i];
        }
    }
}
