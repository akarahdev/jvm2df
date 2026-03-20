import diamondfire.event.PlayerEventHandler;
import diamondfire.value.ItemStack;
import diamondfire.value.List;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var list = List.<Double>of();
        list.add(10.0);
        list.add(20.0);

        var sel = PlayerSelection.defaultTarget();
        for (var elem : list) {
            sel.sendMessage(Text.of(Double.toString(elem)));
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
