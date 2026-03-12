package dev.akarah.jvm2df.codetemplate;

public class Fail {
    public static <A, B> A fail(B input) {
        throw new RuntimeException("Can not do this! :(");
    }
}
