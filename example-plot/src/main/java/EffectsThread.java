import diamondfire.Control;
import diamondfire.presets.ParticleTypes;
import diamondfire.presets.SoundTypes;
import diamondfire.value.Particle;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Sound;

public class EffectsThread extends Thread {
    PlayerSelection selection;

    public EffectsThread(PlayerSelection selection) {
        this.selection = selection;
    }

    @Override
    public void run() {
        while (true) {
            var part = new Particle(ParticleTypes.CRIT);
            Control.debug("meow");
            this.selection.displayParticle(
                    this.selection.location(),
                    part
            );
            this.selection.playSound(
                    new Sound(SoundTypes.BLOCK_AMETHYST_BLOCK_PLACE)
            );
            Control.wait(1);
        }
    }
}
