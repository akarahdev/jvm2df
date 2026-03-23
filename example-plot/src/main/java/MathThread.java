import diamondfire.Control;

public class MathThread extends Thread {
    @Override
    public void run() {
        Control.debug("sin(5) = ");
        Control.debug(Math.sin(5));
        while (true) {
            for (int i = 0; i < 100; i++) {
                PlotStartupHandler.MEOW = new ComplexNumber(
                        PlotStartupHandler.MEOW.real + 1,
                        PlotStartupHandler.MEOW.imag + 1
                );
            }
            Control.debug(PlotStartupHandler.MEOW.toString());
            Control.wait(1);
        }

    }
}
