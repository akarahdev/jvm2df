import diamondfire.Control;
import diamondfire.event.PlayerEventHandler;
import diamondfire.internal.VarItemGen;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Text;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        Control.debug("Starting...");
        Control.wait(40);
        Control.debug("Setting up selection var...");
        var selection = PlayerSelection.defaultTarget();
        Control.wait(18);
        Control.debug(VarItemGen.gameValue("CPU Usage", "Default"));
        Control.wait(40);
        Control.debug("Text gen...");
        var text = Text.of("Hi!");
        Control.wait(18);
        Control.debug(VarItemGen.gameValue("CPU Usage", "Default"));
        Control.wait(40);
        Control.debug("Let's invoke virtually to send...");
        selection.sendMessage(text);
        Control.wait(40);
    }

    @Override
    public void Sneak() {
    }
}