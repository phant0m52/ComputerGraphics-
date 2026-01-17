package engine;

import math.Mat4;
import math.Vec3;

/**
 * Камера с управлением yaw/pitch (как в играх).
 * Считает направления forward/right/up и строит view-матрицу (world -> camera).
 *
 * Оси по умолчанию:
 * - worldUp = (0,1,0)
 * - yaw вращает вокруг Y
 * - pitch вращает вокруг X
 *
 * ВНИМАНИЕ: углы в радианах.
 */
public final class Camera {
    private Vec3 position;
    private double yaw;   // влево/вправо
    private double pitch; // вверх/вниз

    public Camera() {
        this(new Vec3(0.0, 0.0, 0.0), 0.0, 0.0);
    }

    public Camera(Vec3 position, double yaw, double pitch) {
        if (position == null) throw new NullPointerException("position must not be null");
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vec3 getPosition() { return position; }
    public double getYaw() { return yaw; }
    public double getPitch() { return pitch; }

    public void setPosition(Vec3 position) {
        if (position == null) throw new NullPointerException("position must not be null");
        this.position = position;
    }

    public void setYaw(double yaw) { this.yaw = yaw; }

    /** pitch обычно ограничивают, чтобы не переворачивалось (например, [-pi/2+eps, pi/2-eps]) */
    public void setPitch(double pitch) { this.pitch = pitch; }

    public Vec3 getForward() {
        // forward = (cos(pitch)*sin(yaw), sin(pitch), cos(pitch)*cos(yaw))
        double cp = Math.cos(pitch);
        double sp = Math.sin(pitch);
        double cy = Math.cos(yaw);
        double sy = Math.sin(yaw);

        return new Vec3(cp * sy, sp, cp * cy).normalized();
    }

    public Vec3 getRight() {
        Vec3 forward = getForward();
        Vec3 worldUp = new Vec3(0.0, 1.0, 0.0);
        return forward.cross(worldUp).normalized();
    }

    public Vec3 getUp() {
        Vec3 right = getRight();
        Vec3 forward = getForward();
        return right.cross(forward).normalized();
    }

    public void moveForward(double amount) {
        position = position.add(getForward().scale(amount));
    }

    public void moveRight(double amount) {
        position = position.add(getRight().scale(amount));
    }

    public void moveUp(double amount) {
        position = position.add(new Vec3(0.0, 1.0, 0.0).scale(amount));
    }

    /**
     * View matrix (world -> camera).
     * Делается через lookAt: eye = position, target = position + forward, up = worldUp.
     */
    public Mat4 getViewMatrix() {
        Vec3 eye = position;
        Vec3 target = position.add(getForward());
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        return lookAt(eye, target, up);
    }

    /**
     * Классический lookAt (для column-vectors).
     * Строит матрицу вида, которая переводит world -> camera.
     */
    public static Mat4 lookAt(Vec3 eye, Vec3 target, Vec3 up) {
        if (eye == null) throw new NullPointerException("eye must not be null");
        if (target == null) throw new NullPointerException("target must not be null");
        if (up == null) throw new NullPointerException("up must not be null");

        Vec3 f = target.sub(eye).normalized();     // forward
        Vec3 s = f.cross(up).normalized();         // right
        Vec3 u = s.cross(f).normalized();          // true up

        // Матрица вида:
        // [ s.x  s.y  s.z  -dot(s, eye) ]
        // [ u.x  u.y  u.z  -dot(u, eye) ]
        // [ -f.x -f.y -f.z  dot(f, eye) ]
        // [ 0    0    0      1          ]
        double[][] m = new double[4][4];
        m[0][0] = s.x;   m[0][1] = s.y;   m[0][2] = s.z;   m[0][3] = -s.dot(eye);
        m[1][0] = u.x;   m[1][1] = u.y;   m[1][2] = u.z;   m[1][3] = -u.dot(eye);
        m[2][0] = -f.x;  m[2][1] = -f.y;  m[2][2] = -f.z;  m[2][3] =  f.dot(eye);
        m[3][0] = 0.0;   m[3][1] = 0.0;   m[3][2] = 0.0;   m[3][3] = 1.0;

        // ВНИМАНИЕ: чтобы это скомпилилось, тебе нужен способ создать Mat4 из массива.
        // Сейчас конструктор Mat4 private.
        // Решения:
        // 1) добавить в Mat4: public static Mat4 fromArray(double[][] a) { return new Mat4(copy); }
        // 2) или добавить пакетный/публичный конструктор
        // 3) или сделать lookAt внутри Mat4 как static метод
        return Mat4.fromArray(m);
    }
}
