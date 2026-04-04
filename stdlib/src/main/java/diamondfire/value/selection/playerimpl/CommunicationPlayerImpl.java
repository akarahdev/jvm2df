package diamondfire.value.selection.playerimpl;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.value.Text;
import diamondfire.value.selection.Selection;

public interface CommunicationPlayerImpl extends Selection {
    default void sendMessage(Text text) {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction(
                "SendMessage",
                text,
                VarItemGen.tag("Inherit Styles", "False"),
                VarItemGen.tag("Text Value Merging", "Add spaces"),
                VarItemGen.tag("Alignment Mode", "Regular")
        );
        CodeBlocks.selectObject("Reset");
    }

    default void sendTitle(Text title, Text subtitle, int duration, int fadeIn, int fadeOut) {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction("SendMessage", title, subtitle, duration, fadeIn, fadeOut);
        CodeBlocks.selectObject("Reset");
    }

    default void sendActionBar(Text text) {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction(
                "ActionBar",
                text,
                VarItemGen.tag("Inherit Styles", "False"),
                VarItemGen.tag("Text Value Merging", "Add spaces")
        );
        CodeBlocks.selectObject("Reset");
    }

    default void sendBossBar(Text text, double health, String skyEffect, String barColor) {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction(
                " SetBossBar ",
                text,
                health * 1000,
                1000,
                1,
                VarItemGen.tag("Sky Effect", "None", skyEffect),
                VarItemGen.tag("Bar Style", "Solid"),
                VarItemGen.tag("Bar Color", "Purple", barColor)
        );
        CodeBlocks.selectObject("Reset");
    }

    default void removeBossBars() {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction("RemoveBossBar");
        CodeBlocks.selectObject("Reset");
    }
}
