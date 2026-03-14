package diamondfire;

import diamondfire.internal.BlockTagGen;
import diamondfire.internal.CodeBlocks;

public class Control {
    public static void debug(int argument) {
        CodeBlocks.control(
                "PrintDebug",
                argument,
                BlockTagGen.tag("Permission", "Developer", "PrintDebug", "control"),
                BlockTagGen.tag("Text Value Merging", "No Spaces", "PrintDebug", "control"),
                BlockTagGen.tag("Highlighting", "None", "PrintDebug", "control"),
                BlockTagGen.tag("Sound", "Default", "PrintDebug", "control"),
                BlockTagGen.tag("Message Style", "Debug", "PrintDebug", "control")
        );
    }

    public static void wait(int ticks) {
        CodeBlocks.control(
                "Wait",
                ticks,
                BlockTagGen.tag("Time Unit", "Ticks", "Wait", "control")
        );
    }
}
