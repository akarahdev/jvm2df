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

    public ComplexNumber multiply(ComplexNumber other) {
        return new ComplexNumber(
                (this.real * other.real) - (this.imag * other.imag),
                (this.real * other.imag) + (this.imag * other.real)
        );
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(this.real, -this.imag);
    }

    public void debugPrint() {
        Control.debug(this.real);
        Control.debug(this.imag);
    }
}
