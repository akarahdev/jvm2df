import diamondfire.event.PlayerEventHandler;
import diamondfire.value.List;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {

    }

    @Override
    public void Sneak() {
        for (int i = 0; i < 5; i++) {
            var sel = PlayerSelection.defaultTarget();
            var list = List.<Double>of();
            list.add(10.0);
            list.add(20.0);
            for (var elem : list) {
                sel.sendMessage(Text.of(Double.toString(elem)));
            }
        }
        System.gc();
    }
}
