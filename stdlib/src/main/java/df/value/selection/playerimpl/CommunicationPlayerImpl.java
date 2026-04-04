package df.value.selection.playerimpl;

import df.internal.CodeBlocks;
import df.internal.VarItemGen;
import df.value.Text;
import df.value.selection.Selection;
import df.value.tags.BossBarColor;
import df.value.tags.BossBarSkyEffect;

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

    default void sendBossBar(Text text, double health, BossBarSkyEffect skyEffect, BossBarColor barColor) {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction(
                " SetBossBar ",
                text,
                health * 1000,
                1000,
                1,
                VarItemGen.tag("Sky Effect", "None", skyEffect.toString()),
                VarItemGen.tag("Bar Style", "Solid"),
                VarItemGen.tag("Bar Color", "Purple", barColor.toString())
        );
        CodeBlocks.selectObject("Reset");
    }

    default void removeBossBars() {
        CodeBlocks.selectObject("PlayerName", VarItemGen.readField(this, "inner"));
        CodeBlocks.playerAction("RemoveBossBar");
        CodeBlocks.selectObject("Reset");
    }
}
