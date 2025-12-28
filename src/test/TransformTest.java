package test;

import engine.Transform;
import math.Mat4;
import math.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransformTest {
    @Test
    void transform_matrix_is_trs() {
        Transform t = new Transform();
        t.setPosition(Vec3.of(10, 0, 0));
        t.setScale(Vec3.of(2, 2, 2));

        Mat4 m = t.toMatrix();

        // p=(1,0,0) -> scale => (2,0,0) -> translate => (12,0,0)
        Vec3 out = m.transformPoint(Vec3.of(1, 0, 0));
        assertEquals(12.0, out.x, 1e-9);
        assertEquals(0.0, out.y, 1e-9);
        assertEquals(0.0, out.z, 1e-9);
    }
}
