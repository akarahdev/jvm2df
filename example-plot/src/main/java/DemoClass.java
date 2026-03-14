import diamondfire.Control;

public class DemoClass {
    public static void sumTheArrays() {
        int[] a = new int[]{1, 2, 3};
        int[] b = new int[]{4, 5, 6};
        int[] added = addArrays(a, b);

        for(int i = 0; i < added.length; i++) {
            Control.debug(added[i]);
        }
    }

    public static int[] addArrays(int[] a, int[] b) {
        int[] summand = new int[a.length];
        for(int i = 0; i < a.length; i++) {
            summand[i] = a[i] + b[i];
        }
        return summand;
    }
}
