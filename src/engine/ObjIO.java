package engine;

import math.Vec3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Простейший экспорт в Wavefront OBJ (только vertices + triangular faces).
 *
 * applyTransform=false: пишет mesh как есть (local coords)
 * applyTransform=true: применяет Transform (bake) и пишет уже преобразованную геометрию
 */
public final class ObjIO {
    private ObjIO() {}

    public static void writeObj(Mesh mesh, Path path) throws IOException {
        if (mesh == null) throw new NullPointerException("mesh must not be null");
        if (path == null) throw new NullPointerException("path must not be null");

        StringBuilder sb = new StringBuilder();
        for (Vec3 v : mesh.getVertices()) {
            sb.append("v ").append(v.x).append(' ').append(v.y).append(' ').append(v.z).append('\n');
        }

        int[] idx = mesh.getIndices();
        for (int i = 0; i < idx.length; i += 3) {
            // OBJ indices are 1-based
            int a = idx[i] + 1;
            int b = idx[i + 1] + 1;
            int c = idx[i + 2] + 1;
            sb.append("f ").append(a).append(' ').append(b).append(' ').append(c).append('\n');
        }

        Files.write(path, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void writeObj(ModelInstance instance, Path path, boolean applyTransform) throws IOException {
        if (instance == null) throw new NullPointerException("instance must not be null");
        if (!applyTransform) {
            writeObj(instance.getMesh(), path);
            return;
        }
        writeObj(instance.bakedMesh(), path);
    }
}
