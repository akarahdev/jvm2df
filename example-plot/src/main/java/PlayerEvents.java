import diamondfire.Control;
import diamondfire.event.PlayerEventHandler;
import diamondfire.value.bucket.BucketHandle;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    public void Join() {
        var bucket = BucketHandle.primary("hai");
        Control.debug(bucket.load());
        bucket.store(Keys.MY_KEY, "hey");
        Control.debug(bucket.read(Keys.MY_KEY));
        Control.debug(bucket.saveAndUnload());
    }
}