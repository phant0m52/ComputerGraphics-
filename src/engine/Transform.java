package engine;

import math.Mat4;
import math.Vec3;

public final class Transform {
    private Vec3 position = Vec3.of(0, 0, 0);
    private Vec3 rotationRad = Vec3.of(0, 0, 0); // Euler XYZ
    private Vec3 scale = Vec3.of(1, 1, 1);

    public Vec3 getPosition() { return position; }
    public Vec3 getRotationRad() { return rotationRad; }
    public Vec3 getScale() { return scale; }

    public void setPosition(Vec3 p) { position = p; }
    public void setRotationRad(Vec3 r) { rotationRad = r; }
    public void setScale(Vec3 s) { scale = s; }

    // Вектора-столбцы: M = T * Rz * Ry * Rx * S
    public Mat4 toMatrix() {
        Mat4 t  = Mat4.translation(position.x, position.y, position.z);
        Mat4 rx = Mat4.rotationX(rotationRad.x);
        Mat4 ry = Mat4.rotationY(rotationRad.y);
        Mat4 rz = Mat4.rotationZ(rotationRad.z);
        Mat4 s  = Mat4.scale(scale.x, scale.y, scale.z);
        return t.mul(rz).mul(ry).mul(rx).mul(s);
    }
}
