package engine;

import math.Mat4;
import math.MathUtil;
import math.Vec3;

public final class Camera {
    private Vec3 position = Vec3.of(0, 0, -5);
    private double yawRad = 0.0;    // вокруг Y
    private double pitchRad = 0.0;  // вокруг X

    private double fovYRad = Math.toRadians(60);
    private double aspect = 16.0 / 9.0;
    private double near = 0.1;
    private double far = 500.0;

    public Vec3 getPosition() { return position; }
    public void setPosition(Vec3 p) { position = p; }
    public void setAspect(double aspect) { this.aspect = aspect; }

    public void addYawPitch(double dYaw, double dPitch) {
        yawRad += dYaw;
        pitchRad = MathUtil.clamp(pitchRad + dPitch, Math.toRadians(-89), Math.toRadians(89));
    }

    public Vec3 forward() {
        double cy = Math.cos(yawRad), sy = Math.sin(yawRad);
        double cp = Math.cos(pitchRad), sp = Math.sin(pitchRad);
        return Vec3.of(sy * cp, -sp, cy * cp).normalized();
    }

    public Vec3 right() {
        Vec3 f = forward();
        return f.cross(Vec3.of(0, 1, 0)).normalized().mul(-1);
    }

    public Vec3 up() {
        Vec3 r = right();
        Vec3 f = forward();
        return r.cross(f).normalized();
    }

    // View = basis * translation(-pos)
    public Mat4 viewMatrix() {
        Vec3 f = forward();
        Vec3 r = right();
        Vec3 u = up();

        double[][] m = new double[4][4];
        m[0][0] = r.x; m[0][1] = r.y; m[0][2] = r.z;
        m[1][0] = u.x; m[1][1] = u.y; m[1][2] = u.z;
        m[2][0] = -f.x; m[2][1] = -f.y; m[2][2] = -f.z;
        m[3][3] = 1.0;

        Mat4 basis = Mat4.of(m);
        Mat4 trans = Mat4.translation(-position.x, -position.y, -position.z);
        return basis.mul(trans);
    }

    public Mat4 projectionMatrix() {
        double f = 1.0 / Math.tan(fovYRad / 2.0);
        double[][] m = new double[4][4];
        m[0][0] = f / aspect;
        m[1][1] = f;
        m[2][2] = (far + near) / (near - far);
        m[2][3] = (2 * far * near) / (near - far);
        m[3][2] = -1.0;
        return Mat4.of(m);
    }
}
