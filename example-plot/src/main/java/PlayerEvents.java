import df.Control;
import df.event.PlayerEventHandler;
import df.value.Text;
import df.value.selection.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    @SuppressWarnings("all")
    public void Join() {
        var sel = PlayerSelection.defaultTarget();
        switch (Datas.bruh()) {
            case 1 -> {
                sel.sendMessage(Text.of("1! mrow :3"));
                Control.debug("1");
            }
            case 2 -> {
                sel.sendMessage(Text.of("2! mrow :3"));
                Control.debug("2");
            }
            case 3 -> {
                sel.sendMessage(Text.of("3! mrow :3"));
                Control.debug("3");
            }
        }
    }
}