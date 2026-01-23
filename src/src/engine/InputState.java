package engine;

public interface InputState {
    boolean isKeyDown(int keyCode);
    double consumeMouseDeltaX();
    double consumeMouseDeltaY();
    default double consumeMouseWheelDelta() { return 0.0; }
}
