package diamondfire;

import diamondfire.internal.VarItemGen;
import diamondfire.internal.CodeBlocks;

public class PlayerAction {
    public static void sendMessage(String argument) {
        CodeBlocks.playerAction(
                "SendMessage",
                argument,
                VarItemGen.tag("Inherit Styles", "True"),
                VarItemGen.tag("Text Value Merging", "Add spaces"),
                VarItemGen.tag("Alignment Mode", "Regular")
        );
    }
}
