package engine;

import math.Mat4;
import math.Vec3;

public final class Transform {
    private Vec3 position = new Vec3(0, 0, 0);
    private Vec3 rotation = new Vec3(0, 0, 0); // radians
    private Vec3 scale = new Vec3(1, 1, 1);

    public Vec3 getPosition() { return position; }
    public Vec3 getRotation() { return rotation; }
    public Vec3 getScale() { return scale; }

    public void setPosition(Vec3 p) { position = p; }
    public void setRotation(Vec3 r) { rotation = r; }
    public void setScale(Vec3 s) { scale = s; }

    /** Сброс Transform в identity. */
    public void reset() {
        position = new Vec3(0, 0, 0);
        rotation = new Vec3(0, 0, 0);
        scale = new Vec3(1, 1, 1);
    }

    /** Удобный инкремент позиции. */
    public void translate(Vec3 delta) {
        if (delta == null) throw new NullPointerException("delta must not be null");
        position = position.add(delta);
    }

    /** Удобный инкремент вращения (в радианах). */
    public void rotate(Vec3 deltaRadians) {
        if (deltaRadians == null) throw new NullPointerException("deltaRadians must not be null");
        rotation = rotation.add(deltaRadians);
    }

    /** Удобный инкремент масштаба (покомпонентно). */
    public void scaleBy(Vec3 factor) {
        if (factor == null) throw new NullPointerException("factor must not be null");
        scale = new Vec3(scale.x * factor.x, scale.y * factor.y, scale.z * factor.z);
    }

    public Mat4 toMatrix() {
        Mat4 t  = Mat4.translate(position.x, position.y, position.z);
        Mat4 ry = Mat4.rotateY(rotation.y);
        Mat4 rx = Mat4.rotateX(rotation.x);
        Mat4 rz = Mat4.rotateZ(rotation.z);
        Mat4 s  = Mat4.scale(scale.x, scale.y, scale.z);
        return t.multiply(rz).multiply(ry).multiply(rx).multiply(s);
    }
}
