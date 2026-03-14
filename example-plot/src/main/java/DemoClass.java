import diamondfire.Control;
import diamondfire.internal.BlockTagGen;
import diamondfire.internal.CodeBlocks;

public class DemoClass {
    public static int sumTheArrays() {
        int[] a = new int[]{1, 2, 3};
        int[] b = new int[]{4, 5, 6};
        int[] added = addArrays(a, b);

        for(int i = 0; i < added.length; i++) {
            Control.debug(added[i]);
        }

        return 0;
    }

    public static int[] addArrays(int[] a, int[] b) {
        int[] summand = new int[a.length];
        for(int i = 0; i < a.length; i++) {
            summand[i] = a[i] + b[i];
        }
        return summand;
    }
}
