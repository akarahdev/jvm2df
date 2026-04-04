import diamondfire.event.PlayerEventHandler;
import diamondfire.value.Text;
import diamondfire.value.selection.PlayerSelection;
import diamondfire.value.tags.BossBarColor;
import diamondfire.value.tags.BossBarSkyEffect;

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