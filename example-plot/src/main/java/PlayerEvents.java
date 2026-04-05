import df.event.PlayerEventHandler;
import df.value.Text;
import df.value.selection.PlayerSelection;
import df.value.tags.BossBarColor;
import df.value.tags.BossBarSkyEffect;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    @SuppressWarnings("all")
    public void Join() {
        var sel = PlayerSelection.defaultTarget();
        sel.sendBossBar(
                Text.of("mrow"),
                1,
                BossBarSkyEffect.BOTH,
                BossBarColor.BLUE
        );
    }
}