import diamondfire.Control;
import diamondfire.event.PlayerEventHandler;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {

    }

    @Override
    public void Sneak() {
        while (true) {
            var rt = Runtime.getRuntime();
            for (int i = 0; i < 400; i++) {
                var a = new Object();
                Control.debug(a);
                var free = rt.freeMemory();
                if (free <= 4000) {
                    Control.debug("gc!");
                    Control.debug(i);
                    rt.gc();
                }
            }
            Control.debug(rt.freeMemory());
            Control.wait(1);
        }
    }
}
