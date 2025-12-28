package engine;

import math.Mat4;
import math.Vec3;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class ObjIO {
    private ObjIO() {}

    public static Mesh load(File file) throws IOException {
        Mesh mesh = new Mesh();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("v ")) {
                    String[] p = line.split("\\s+");
                    double x = Double.parseDouble(p[1]);
                    double y = Double.parseDouble(p[2]);
                    double z = Double.parseDouble(p[3]);
                    mesh.vertices.add(Vec3.of(x, y, z));
                } else if (line.startsWith("f ")) {
                    // f v1 v2 v3 ... (берём треангуляцию "веером")
                    String[] p = line.split("\\s+");
                    int[] idx = new int[p.length - 1];
                    for (int i = 1; i < p.length; i++) {
                        // поддержка формата: v / v/vt / v/vt/vn / v//vn
                        String token = p[i];
                        String[] parts = token.split("/");
                        int vi = Integer.parseInt(parts[0]);
                        if (vi < 0) vi = mesh.vertices.size() + vi + 1; // OBJ отрицательные индексы
                        idx[i - 1] = vi - 1; // OBJ 1-based
                    }
                    // fan triangulation
                    for (int i = 1; i + 1 < idx.length; i++) {
                        mesh.faces.add(new int[]{idx[0], idx[i], idx[i + 1]});
                    }
                }
            }
        }
        return mesh;
    }

    public static void save(File file, Mesh mesh, boolean applyTransform) throws IOException {
        Mat4 m = applyTransform ? mesh.transform.toMatrix() : Mat4.identity();

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.println("# saved by mini3d");
            for (Vec3 v : mesh.vertices) {
                Vec3 w = m.transformPoint(v);
                out.printf("v %.9f %.9f %.9f%n", w.x, w.y, w.z);
            }
            for (int[] f : mesh.faces) {
                // OBJ 1-based
                out.printf("f %d %d %d%n", f[0] + 1, f[1] + 1, f[2] + 1);
            }
        }
    }
}
