public class DemoClass {
    public static int sumTheArrays() {
        int[] a = new int[]{1, 2, 3};
        int[] b = new int[]{4, 5, 6};
        int[] added = addArrays(a, b);

        int sum = 0;
        for(int i = 0; i < added.length; i++) {
            sum += added[i];
        }
        return sum;
    }

    public static int[] addArrays(int[] a, int[] b) {
        int[] summand = new int[a.length];
        for(int i = 0; i < a.length; i++) {
            summand[i] = a[i] + b[i];
        }
        return summand;
    }
}
