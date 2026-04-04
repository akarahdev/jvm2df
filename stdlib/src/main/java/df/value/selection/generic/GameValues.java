package df.value.selection.generic;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.value.Location;

public interface GameValues {
    void setSelection();

    default Location location() {
        this.setSelection();
        Location out = VarItemGen.gameValue("Location", "Selection");
        CodeBlocks.selectObject("Reset");
        return out;
    }

    default int size() {
        this.setSelection();
        int out = VarItemGen.gameValue("Selection Size", "Default");
        CodeBlocks.selectObject("Reset");
        return out;
    }
}
