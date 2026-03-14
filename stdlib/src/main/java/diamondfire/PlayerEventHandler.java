package diamondfire;

public abstract class PlayerEventHandler {
    public native void Join();
    public native void Leave();
    public native void LeftClick();
    public native void RightClick();
    public native void Sneak();
    public native void PlaceBlock();
    public native void BreakBlock();
}
