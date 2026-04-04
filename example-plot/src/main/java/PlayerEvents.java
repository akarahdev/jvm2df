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
        sel.sendMessage(Text.of(Datas.A.toString()));
        sel.sendBossBar(
                Text.of("Bossbar :3"),
                0.5,
                BossBarSkyEffect.CREATE_FOG,
                BossBarColor.RED
        );
    }
}