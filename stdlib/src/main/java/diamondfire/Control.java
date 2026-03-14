package diamondfire;

import diamondfire.internal.VarItemGen;
import diamondfire.internal.CodeBlocks;

public class Control {
    public static void debug(Object argument) {
        CodeBlocks.control(
                "PrintDebug",
                argument,
                VarItemGen.tag("Permission", "Developer", "PrintDebug", "control"),
                VarItemGen.tag("Text Value Merging", "No Spaces", "PrintDebug", "control"),
                VarItemGen.tag("Highlighting", "None", "PrintDebug", "control"),
                VarItemGen.tag("Sound", "Default", "PrintDebug", "control"),
                VarItemGen.tag("Message Style", "Debug", "PrintDebug", "control")
        );
    }

    public static void wait(int ticks) {
        CodeBlocks.control(
                "Wait",
                ticks,
                VarItemGen.tag("Time Unit", "Ticks", "Wait", "control")
        );
    }
}
