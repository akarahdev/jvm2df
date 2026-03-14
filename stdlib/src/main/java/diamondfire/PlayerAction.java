package diamondfire;

import diamondfire.internal.BlockTagGen;
import diamondfire.internal.CodeBlocks;

public class PlayerAction {
    public static void sendMessage(Object argument) {
        CodeBlocks.playerAction(
                "SendMessage",
                argument,
                BlockTagGen.tag("Inherit Styles", "True", "SendMessage", "player_action"),
                BlockTagGen.tag("Text Value Merging", "Add spaces", "SendMessage", "player_action"),
                BlockTagGen.tag("Alignment Mode", "Regular", "SendMessage", "player_action")
        );
    }
}
