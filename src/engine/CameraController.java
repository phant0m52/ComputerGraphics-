package engine;

import java.awt.event.KeyEvent;

/**
 * Управление камерой "как в играх":
 * - WASD: вперёд/назад/влево/вправо
 * - Space/Ctrl: вверх/вниз
 * - Shift: ускорение
 * - Мышь: yaw/pitch
 *
 * Camera хранит углы в радианах.
 */
public final class CameraController {

    private final Camera camera;

    /** Скорость перемещения (units/second). */
    public double moveSpeed = 4.0;

    /** Множитель скорости при зажатом Shift. */
    public double fastMultiplier = 3.0;

    /** Чувствительность мыши (radians per pixel). */
    public double mouseSensitivity = 0.0025;

    /** Ограничение pitch (в радианах), чтобы не переворачивалась камера. */
    public double pitchLimit = Math.toRadians(89.0);

    /** Инвертировать вертикаль мыши. */
    public boolean invertY = false;

    /** Если true, колёсико двигает камеру вперёд/назад. */
    public boolean wheelMovesForward = true;

    /** Сколько units на один "щелчок" колеса (обычно wheelRotation ~= 1). */
    public double wheelStep = 1.0;

    public CameraController(Camera camera) {
        if (camera == null) throw new NullPointerException("camera must not be null");
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }

    /** Вызывай раз в кадр. dtSeconds — в секундах. */
    public void update(double dtSeconds, InputState input) {
        if (input == null) throw new NullPointerException("input must not be null");

        // 1) Поворот мышью
        double dx = input.consumeMouseDeltaX();
        double dy = input.consumeMouseDeltaY();
        if (dx != 0.0 || dy != 0.0) {
            double yaw = camera.getYaw() + dx * mouseSensitivity;
            double pitchDelta = (invertY ? dy : -dy) * mouseSensitivity;
            double pitch = clamp(camera.getPitch() + pitchDelta, -pitchLimit, pitchLimit);
            camera.setYaw(yaw);
            camera.setPitch(pitch);
        }

        // 2) Передвижение (клава)
        double speed = moveSpeed;
        if (input.isKeyDown(KeyEvent.VK_SHIFT)) speed *= fastMultiplier;
        double step = speed * dtSeconds;

        if (input.isKeyDown(KeyEvent.VK_W)) camera.moveForward(step);
        if (input.isKeyDown(KeyEvent.VK_S)) camera.moveForward(-step);
        if (input.isKeyDown(KeyEvent.VK_D)) camera.moveRight(step);
        if (input.isKeyDown(KeyEvent.VK_A)) camera.moveRight(-step);

        if (input.isKeyDown(KeyEvent.VK_SPACE)) camera.moveUp(step);
        if (input.isKeyDown(KeyEvent.VK_CONTROL)) camera.moveUp(-step);

        // 3) Колесо (опционально)
        double wheel = input.consumeMouseWheelDelta();
        if (wheelMovesForward && wheel != 0.0) {
            // В AWT wheel > 0 обычно "вниз" => движение назад
            camera.moveForward(-wheel * wheelStep);
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return (v < lo) ? lo : Math.min(v, hi);
    }
}
