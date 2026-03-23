import diamondfire.Control;

public class PlotStartupHandler {
    public static ComplexNumber MEOW = new ComplexNumber(15, 20);

    static {
        Control.debug("I run on plot startup! Yay");
    }
}
