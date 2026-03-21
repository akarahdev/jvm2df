package java.lang;

import diamondfire.internal.VarItemGen;

public class System {
    public static void gc() {
        VarItemGen.runGc();
    }
}
