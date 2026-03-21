package java.lang;

import diamondfire.internal.VarItemGen;

public class Runtime {
    private Runtime() {
    }

    public static Runtime getRuntime() {
        return new Runtime();
    }

    public void gc() {
        VarItemGen.runGc();
    }

    public long maxMemory() {
        return 5000;
    }

    public long totalMemory() {
        return VarItemGen.gcAllocationCount();
    }

    public long freeMemory() {
        return 5000 - VarItemGen.gcAllocationCount();
    }
}
