package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;
import diamondfire.internal.annotation.NativeValue;

@NativeValue
public class Selection {

    private Selection() {

    }

    public static Selection defaultTarget() {
        return CodeBlocks.setVar(
                "CreateList",
                VarItemGen.lineVar(),
                VarItemGen.gameValue("UUID", "Default")
        );
    }

    public void sendMessage(Text text) {
        CodeBlocks.selectObject("PlayerName", this);
        CodeBlocks.playerAction("SendMessage", text, VarItemGen.tag("Inherit Styles", "True"), VarItemGen.tag("Text Value Merging", "Add spaces"), VarItemGen.tag("Alignment Mode", "Regular"));
        CodeBlocks.selectObject("Reset");
    }

    public void giveItem(ItemStack item) {
        CodeBlocks.selectObject("PlayerName", this);
        CodeBlocks.playerAction("GiveItems", item);
        CodeBlocks.selectObject("Reset");
    }

    public boolean isHolding(ItemStack itemStack) {
        boolean out = false;
        CodeBlocks.selectObject("PlayerName", this);
        CodeBlocks.ifPlayer("IsHolding", itemStack, VarItemGen.tag("Hand Slot", "Either hand"));
        CodeBlocks.openNormal();
        out = true;
        CodeBlocks.closeNormal();
        CodeBlocks.selectObject("Reset");
        return out;
    }

    public void launchProjectile(ItemStack itemStack) {
        CodeBlocks.selectObject("PlayerName", this);
        CodeBlocks.playerAction("LaunchProj", itemStack);
        CodeBlocks.selectObject("Reset");
    }
}
