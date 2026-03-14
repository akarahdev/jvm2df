package diamondfire;

import diamondfire.internal.VarItemGen;
import diamondfire.internal.CodeBlocks;

public class PlayerAction {
    public static void sendMessage(Object argument) {
        CodeBlocks.playerAction(
                "SendMessage",
                argument,
                VarItemGen.tag("Inherit Styles", "True", "SendMessage", "player_action"),
                VarItemGen.tag("Text Value Merging", "Add spaces", "SendMessage", "player_action"),
                VarItemGen.tag("Alignment Mode", "Regular", "SendMessage", "player_action")
        );
    }
}
