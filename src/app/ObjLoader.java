package engine;

import math.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ObjLoader {
    private ObjLoader() {}

    public static Mesh load(Path objPath) throws IOException {
        List<Vec3> verts = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();

        for (String line : Files.readAllLines(objPath)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("v ")) {
                String[] p = line.split("\\s+");
                double x = Double.parseDouble(p[1]);
                double y = Double.parseDouble(p[2]);
                double z = Double.parseDouble(p[3]);
                verts.add(new Vec3(x, y, z));
            } else if (line.startsWith("f ")) {
                String[] p = line.split("\\s+");
                int n = p.length - 1;

                int v0 = parseIndex(p[1]);
                for (int i = 2; i < n; i++) {
                    int v1 = parseIndex(p[i]);
                    int v2 = parseIndex(p[i + 1]);
                    idx.add(v0);
                    idx.add(v1);
                    idx.add(v2);
                }
            }
        }

        int[] indices = new int[idx.size()];
        for (int i = 0; i < idx.size(); i++) indices[i] = idx.get(i);

        return new Mesh(verts, indices);
    }

    private static int parseIndex(String token) {
        // "12" или "12/3" или "12/3/7"
        String[] parts = token.split("/");
        int vi = Integer.parseInt(parts[0]);
        return vi - 1; // OBJ 1-based
    }
}