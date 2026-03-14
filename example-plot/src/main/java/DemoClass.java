public class DemoClass {
    public void testArrays() {
        var arr = new int[]{1, 2, 3};
        arr[1] = 10;
        var t = arr[2] + 5;
    }

    public int sumTo15() {
        int summand = 0;
        for(int i = 0; i < 20; i++) {
            summand += i;
        }
        return summand;
    }
}
