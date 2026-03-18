package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class PlayerSelection {
    Object inner;

    private PlayerSelection() {

    }

    public static PlayerSelection defaultTarget() {
        var s = new PlayerSelection();
        s.inner = CodeBlocks.setVar(
                "CreateList",
                VarItemGen.lineVar(),
                VarItemGen.gameValue("UUID", "Default")
        );
        return s;
    }

    public void sendMessage(Text text) {
        CodeBlocks.selectObject("PlayerName", this.inner);
        CodeBlocks.playerAction("SendMessage", text, VarItemGen.tag("Inherit Styles", "True"), VarItemGen.tag("Text Value Merging", "Add spaces"), VarItemGen.tag("Alignment Mode", "Regular"));
        CodeBlocks.selectObject("Reset");
    }

    public void giveItem(ItemStack item) {
        CodeBlocks.selectObject("PlayerName", this.inner);
        CodeBlocks.playerAction("GiveItems", item);
        CodeBlocks.selectObject("Reset");
    }

    public boolean isHolding(ItemStack itemStack) {
        CodeBlocks.selectObject("PlayerName", this.inner);
        CodeBlocks.ifPlayer("IsHolding", itemStack, VarItemGen.tag("Hand Slot", "Either hand"));
        CodeBlocks.openNormal();
        CodeBlocks.ret(true);
        CodeBlocks.closeNormal();
        CodeBlocks.selectObject("Reset");
        return false;
    }

    public void launchProjectile(ItemStack itemStack) {
        CodeBlocks.selectObject("PlayerName", this.inner);
        CodeBlocks.playerAction("LaunchProj", itemStack);
        CodeBlocks.selectObject("Reset");
    }
}
