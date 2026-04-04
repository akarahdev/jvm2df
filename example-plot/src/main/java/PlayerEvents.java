import diamondfire.event.PlayerEventHandler;
import diamondfire.value.Text;
import diamondfire.value.selection.PlayerSelection;

public class PlayerEvents extends PlayerEventHandler {
    @Override
    @SuppressWarnings("all")
    public void Join() {
        int[] arr = {1, 2, 3};
        int[] arr2 = arr.clone();
        for (int i = 0; i < arr2.length; i++) {
            PlayerSelection.defaultTarget().sendMessage(Text.of(arr2[i]));
        }
    }
}