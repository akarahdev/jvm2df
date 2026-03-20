import diamondfire.Control;
import diamondfire.event.PlayerEventHandler;
import diamondfire.value.ItemStack;
import diamondfire.value.List;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var complex = new ComplexNumber(5, 10);

        var sel = PlayerSelection.defaultTarget();
        sel.sendMessage(Text.of(complex.toString()));
        sel.sendMessage(Text.of(complex.getClass().toString()));

        var elements = complex.elements();
        Control.debug(elements);
        Control.debug(elements[0]);
        Control.debug(elements[1]);

        var elementsList = List.byArray(elements);
        Control.debug(elementsList);
        Control.debug(elementsList.get(0));
        Control.debug(elementsList.get(1));

    }

    @Override
    public void RightClick() {
        var sel = PlayerSelection.defaultTarget();
        if (sel.isHolding(ItemStack.of("diamond"))) {
            sel.launchProjectile(ItemStack.of("fire_charge"));
        }
    }
}
