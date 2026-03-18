import diamondfire.event.PlayerEventHandler;
import diamondfire.value.ItemStack;
import diamondfire.value.List;
import diamondfire.value.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var list = List.<ItemStack>of();
        list.add(ItemStack.of("diamond"));
        list.add(ItemStack.of("diamond_sword"));

        var sel = PlayerSelection.defaultTarget();

        for (int i = 0; i < list.length(); i++) {
            sel.giveItem(list.get(i));
        }
    }

    @Override
    public void RightClick() {
        var sel = PlayerSelection.defaultTarget();
        if (sel.isHolding(ItemStack.of("diamond"))) {
            sel.launchProjectile(ItemStack.of("fire_charge"));
        }
    }
}
