package java.lang;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class Math {
    public static final float E = 2.7182818284590452354f;
    public static final float PI = 3.14159265358979323846f;

    public static double sin(double a) {
        return CodeBlocks.setVar(
                "Sine",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Sine Variant", "Sine"),
                VarItemGen.tag("Input", "Radians")
        );
    }

    public static double cos(double a) {
        return CodeBlocks.setVar(
                "Cosine",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Cosine Variant", "Cosine"),
                VarItemGen.tag("Input", "Radians")
        );
    }

    public static double tan(double a) {
        return CodeBlocks.setVar(
                "Tangent",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Tangent Variant", "Tangent"),
                VarItemGen.tag("Input", "Radians")
        );
    }

    public static double asin(double a) {
        return CodeBlocks.setVar(
                "Sine",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Sine Variant", "Inverse sine (arcsine)"),
                VarItemGen.tag("Input", "Radians")
        );
    }

    public static double acos(double a) {
        return CodeBlocks.setVar(
                "Cosine",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Cosine Variant", "Inverse cosine (arccosine)"),
                VarItemGen.tag("Input", "Radians")
        );
    }

    public static double atan(double a) {
        return CodeBlocks.setVar(
                "Tangent",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Tangent Variant", "Inverse tangent (arctangent)"),
                VarItemGen.tag("Input", "Radians")
        );
    }

    public static double exp(double a) {
        return CodeBlocks.setVar(
                "Exponent",
                VarItemGen.lineVar(),
                a,
                E
        );
    }

    public static double log(double a) {
        return CodeBlocks.setVar(
                "Logarithm",
                VarItemGen.lineVar(),
                a,
                E
        );
    }

    public static double sqrt(double a) {
        return CodeBlocks.setVar(
                "Root",
                VarItemGen.lineVar(),
                a,
                2
        );
    }

    public static double ceil(double a) {
        return CodeBlocks.setVar(
                "RoundNumber",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Round Mode", "Ceiling")
        );
    }

    public static double floor(double a) {
        return CodeBlocks.setVar(
                "RoundNumber",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Round Mode", "Floor")
        );
    }

    public static int round(float a) {
        return CodeBlocks.setVar(
                "RoundNumber",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Round Mode", "Nearest")
        );
    }

    public static long round(double a) {
        return CodeBlocks.setVar(
                "RoundNumber",
                VarItemGen.lineVar(),
                a,
                VarItemGen.tag("Round Mode", "Nearest")
        );
    }

    public static double atan2(double a, double b) {
        return CodeBlocks.setVar(
                "ArcTangent2",
                VarItemGen.lineVar(),
                a,
                b,
                VarItemGen.tag("Output Type", "Radians")
        );
    }

    public static double random() {
        return CodeBlocks.setVar(
                "RandomNumber",
                VarItemGen.lineVar(),
                0,
                1,
                VarItemGen.tag("Rounding Mode", "Decimal number")
        );
    }

    public static int abs(int a) {
        return CodeBlocks.setVar(
                "AbsoluteValue",
                VarItemGen.lineVar(),
                a
        );
    }

    public static long abs(long a) {
        return CodeBlocks.setVar(
                "AbsoluteValue",
                VarItemGen.lineVar(),
                a
        );
    }

    public static float abs(float a) {
        return CodeBlocks.setVar(
                "AbsoluteValue",
                VarItemGen.lineVar(),
                a
        );
    }

    public static double abs(double a) {
        return CodeBlocks.setVar(
                "AbsoluteValue",
                VarItemGen.lineVar(),
                a
        );
    }

    public static int max(int a, int b) {
        return CodeBlocks.setVar(
                "MaxNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }

    public static long max(long a, long b) {
        return CodeBlocks.setVar(
                "MaxNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }

    public static float max(float a, float b) {
        return CodeBlocks.setVar(
                "MaxNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }

    public static double max(double a, double b) {
        return CodeBlocks.setVar(
                "MaxNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }

    public static int min(int a, int b) {
        return CodeBlocks.setVar(
                "MinNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }

    public static long min(long a, long b) {
        return CodeBlocks.setVar(
                "MinNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }

    public static float min(float a, float b) {
        return CodeBlocks.setVar(
                "MinNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }

    public static double min(double a, double b) {
        return CodeBlocks.setVar(
                "MinNumber",
                VarItemGen.lineVar(),
                a,
                b
        );
    }
}
