package df;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;

public class Control {
    public static void debug(Object argument) {
        CodeBlocks.control(
                "PrintDebug",
                argument,
                VarItemGen.tag("Permission", "Developer"),
                VarItemGen.tag("Text Value Merging", "No Spaces"),
                VarItemGen.tag("Highlighting", "None"),
                VarItemGen.tag("Sound", "Default"),
                VarItemGen.tag("Message Style", "Debug")
        );
    }

    public static void wait(int ticks) {
        CodeBlocks.control(
                "Wait",
                ticks,
                VarItemGen.tag("Time Unit", "Ticks")
        );
    }

    public static void panic(String message) {
        CodeBlocks.control(
                "PrintDebug",
                message,
                VarItemGen.tag("Permission", "Developer"),
                VarItemGen.tag("Text Value Merging", "No Spaces"),
                VarItemGen.tag("Highlighting", "None"),
                VarItemGen.tag("Sound", "Error"),
                VarItemGen.tag("Message Style", "Error")
        );
        CodeBlocks.control(
                "End"
        );
    }
}
