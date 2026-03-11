import diamondfire.Control;
import diamondfire.PlayerEventHandler;

public class PlayerEvents implements PlayerEventHandler {
    @Override
    public void playerJoin() {
        for(int i = 0; i < 10; i++) {
            Control.log("Hello!");
            if(Control.truth()) {
                Control.log("This is the truth!");
            } else {
                Control.log("How?");
            }
        }
    }
}
