package diamondfire.value;

import diamondfire.internal.CodeBlocks;
import diamondfire.internal.VarItemGen;

public class PlayerSelection {
    Object inner;

    private PlayerSelection() {

    }

    public static PlayerSelection defaultTarget() {
        var s = new PlayerSelection();
        s.inner = CodeBlocks.setVarR(
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

    public void playSound(Sound sound) {
        CodeBlocks.selectObject("PlayerName", this.inner);
        CodeBlocks.playerAction(
                "PlaySound",
                sound.inner,
                VarItemGen.tag("Sound Source", "Master")
        );
        CodeBlocks.selectObject("Reset");
    }

    public void displayParticle(Location location, Particle particle) {
        CodeBlocks.selectObject("PlayerName", this.inner);
        CodeBlocks.playerAction(
                "Particle",
                particle.inner,
                location
        );
        CodeBlocks.selectObject("Reset");
    }

    public Location location() {
        CodeBlocks.selectObject("PlayerName", this.inner);
        var out = (Location) CodeBlocks.setVarR(
                "=",
                VarItemGen.lineVar(),
                VarItemGen.gameValue("Location", "Selection")
        );
        CodeBlocks.selectObject("Reset");
        return out;
    }
}
