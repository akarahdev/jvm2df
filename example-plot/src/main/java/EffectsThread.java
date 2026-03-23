import diamondfire.Control;
import diamondfire.presets.ParticleTypes;
import diamondfire.presets.SoundTypes;
import diamondfire.value.Particle;
import diamondfire.value.PlayerSelection;
import diamondfire.value.Sound;

public class EffectsThread extends Thread {
    PlayerSelection selection;

    private final Particle PARTICLE = new Particle(ParticleTypes.CRIT);
    private final Sound SOUND = new Sound(SoundTypes.BLOCK_AMETHYST_BLOCK_PLACE);

    public EffectsThread(PlayerSelection selection) {
        this.selection = selection;
    }

    @Override
    public void run() {
        while (true) {
            this.selection.displayParticle(this.selection.location(), PARTICLE);
            this.selection.playSound(SOUND);
            Control.wait(1);
        }
    }
}
