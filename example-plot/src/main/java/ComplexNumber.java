public class ComplexNumber {
    double real;
    double imag;

    public ComplexNumber(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    @Override
    public String toString() {
        return Double.toString(real).concat("+").concat(Double.toString(imag)).concat("i");
    }
}
