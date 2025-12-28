package test;

import math.Mat4;
import math.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Mat4Test {
    private static void assertVec3Close(Vec3 a, Vec3 b, double eps) {
        assertTrue(Math.abs(a.x - b.x) < eps, "x");
        assertTrue(Math.abs(a.y - b.y) < eps, "y");
        assertTrue(Math.abs(a.z - b.z) < eps, "z");
    }

    @Test
    void translation_moves_point() {
        Mat4 t = Mat4.translation(10, 0, 0);
        assertVec3Close(t.transformPoint(Vec3.of(1, 2, 3)), Vec3.of(11, 2, 3), 1e-9);
    }

    @Test
    void translation_does_not_affect_direction() {
        Mat4 t = Mat4.translation(5, 5, 5);
        assertVec3Close(t.transformDirection(Vec3.of(1, 0, 0)), Vec3.of(1, 0, 0), 1e-9);
    }

    @Test
    void composition_order_for_column_vectors() {
        // сначала scale, потом translate => total = T * S
        Mat4 s = Mat4.scale(2, 2, 2);
        Mat4 t = Mat4.translation(10, 0, 0);

        Mat4 total = t.mul(s);
        assertVec3Close(total.transformPoint(Vec3.of(1, 0, 0)), Vec3.of(12, 0, 0), 1e-9);
    }

    @Test
    void rotation_z_90_deg() {
        Mat4 rz = Mat4.rotationZ(Math.toRadians(90));
        assertVec3Close(rz.transformPoint(Vec3.of(1, 0, 0)), Vec3.of(0, 1, 0), 1e-9);
    }
}
