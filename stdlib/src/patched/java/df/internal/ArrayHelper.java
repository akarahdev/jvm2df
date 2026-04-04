package df.internal;

public class ArrayHelper {
    public static Object clone(Object arr) {
        if (arr instanceof Object[] array) return array.clone();
        if (arr instanceof int[] array) return array.clone();
        if (arr instanceof long[] array) return array.clone();
        if (arr instanceof short[] array) return array.clone();
        if (arr instanceof byte[] array) return array.clone();
        if (arr instanceof char[] array) return array.clone();
        if (arr instanceof boolean[] array) return array.clone();
        if (arr instanceof float[] array) return array.clone();
        if (arr instanceof double[] array) return array.clone();
        return new Object();
    }
}
