import diamondfire.Control;

public final class ComplexNumber {
    double real;
    double imag;

    public ComplexNumber(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public ComplexNumber add(ComplexNumber right) {
        return new ComplexNumber(this.real + right.real, this.imag + right.imag);
    }

    public void debugPrint() {
        Control.debug(this.real);
        Control.debug(this.imag);
    }
}
