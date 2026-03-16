import diamondfire.event.PlayerEventHandler;
import diamondfire.value.ItemStack;
import diamondfire.value.Selection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var sel = Selection.defaultTarget();
        sel.sendMessage(Text.of("<gradient:#777777:#cccccc>Hello there!"));
        sel.giveItem(ItemStack.of("diamond"));
    }

    @Override
    public void RightClick() {
        var sel = Selection.defaultTarget();
        if (sel.isHolding(ItemStack.of("diamond"))) {
            sel.launchProjectile(ItemStack.of("fire_charge"));
        }
    }
}
