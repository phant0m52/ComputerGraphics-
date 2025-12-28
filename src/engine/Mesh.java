package engine;

import math.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class Mesh {
    public final List<Vec3> vertices = new ArrayList<>();
    public final List<int[]> faces = new ArrayList<>(); // каждая грань: int[3] индексы вершин (0-based)

    public final Transform transform = new Transform();

    public static Mesh cube() {
        Mesh m = new Mesh();
        // 8 вершин
        m.vertices.add(Vec3.of(-1, -1, -1));
        m.vertices.add(Vec3.of( 1, -1, -1));
        m.vertices.add(Vec3.of( 1,  1, -1));
        m.vertices.add(Vec3.of(-1,  1, -1));
        m.vertices.add(Vec3.of(-1, -1,  1));
        m.vertices.add(Vec3.of( 1, -1,  1));
        m.vertices.add(Vec3.of( 1,  1,  1));
        m.vertices.add(Vec3.of(-1,  1,  1));

        // 12 треугольников (2 на грань)
        int[][] f = {
                {0,1,2},{0,2,3}, // back
                {4,6,5},{4,7,6}, // front
                {0,4,5},{0,5,1}, // bottom
                {3,2,6},{3,6,7}, // top
                {0,3,7},{0,7,4}, // left
                {1,5,6},{1,6,2}  // right
        };
        for (int[] tri : f) m.faces.add(tri);
        return m;
    }
}
