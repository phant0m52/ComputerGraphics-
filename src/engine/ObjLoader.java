package engine;

import math.Vec2;
import math.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Очень простой OBJ loader:
 * - поддерживает v, vt, vn, f
 * - f может быть с любым количеством вершин (триангуляция "веером")
 * - вершины в OBJ могут ссылаться на разные vt/vn, поэтому здесь делаем "склейку" в один индекс по ключу (v, vt).
 *
 * Нормали мы всё равно пересчитываем после загрузки, потому что файлу не доверяем.
 */
public final class ObjLoader {
    private ObjLoader() {}

    public static Mesh load(Path objPath) throws IOException {
        List<Vec3> posSrc = new ArrayList<>();
        List<Vec2> uvSrc = new ArrayList<>();
        List<Vec3> nrmSrc = new ArrayList<>();

        // итоговые списки (после "склейки" индексов)
        List<Vec3> pos = new ArrayList<>();
        List<Vec2> uv = new ArrayList<>();
        List<Vec3> nrm = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();

        // key: (vIndex, vtIndex) -> newVertexIndex
        Map<VertexKey, Integer> map = new HashMap<>();

        for (String line : Files.readAllLines(objPath)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("v ")) {
                String[] p = line.split("\\s+");
                double x = Double.parseDouble(p[1]);
                double y = Double.parseDouble(p[2]);
                double z = Double.parseDouble(p[3]);
                posSrc.add(new Vec3(x, y, z));
            } else if (line.startsWith("vt ")) {
                String[] p = line.split("\\s+");
                double u = Double.parseDouble(p[1]);
                double v = Double.parseDouble(p[2]);
                uvSrc.add(new Vec2(u, v));
            } else if (line.startsWith("vn ")) {
                String[] p = line.split("\\s+");
                double x = Double.parseDouble(p[1]);
                double y = Double.parseDouble(p[2]);
                double z = Double.parseDouble(p[3]);
                nrmSrc.add(new Vec3(x, y, z));
            } else if (line.startsWith("f ")) {
                String[] p = line.split("\\s+");
                int n = p.length - 1;
                if (n < 3) continue;

                int i0 = resolveVertex(p[1], posSrc, uvSrc, nrmSrc, pos, uv, nrm, map);
                for (int i = 2; i < n; i++) {
                    int i1 = resolveVertex(p[i], posSrc, uvSrc, nrmSrc, pos, uv, nrm, map);
                    int i2 = resolveVertex(p[i + 1], posSrc, uvSrc, nrmSrc, pos, uv, nrm, map);
                    idx.add(i0);
                    idx.add(i1);
                    idx.add(i2);
                }
            }
        }

        int[] indices = new int[idx.size()];
        for (int i = 0; i < idx.size(); i++) indices[i] = idx.get(i);

        Mesh mesh = new Mesh(pos, uv, nrm, indices);
        // ВАЖНО: пересчитываем нормали всегда
        return mesh.recalculateNormals();
    }

    private static int resolveVertex(
            String token,
            List<Vec3> posSrc,
            List<Vec2> uvSrc,
            List<Vec3> nrmSrc,
            List<Vec3> posOut,
            List<Vec2> uvOut,
            List<Vec3> nrmOut,
            Map<VertexKey, Integer> map
    ) {
        // token: "v", "v/vt", "v//vn", "v/vt/vn"
        String[] parts = token.split("/");
        int vi = parseObjIndex(parts[0], posSrc.size());

        int vti = -1;
        int vni = -1;
        if (parts.length >= 2 && !parts[1].isEmpty()) vti = parseObjIndex(parts[1], uvSrc.size());
        if (parts.length >= 3 && !parts[2].isEmpty()) vni = parseObjIndex(parts[2], nrmSrc.size());

        VertexKey key = new VertexKey(vi, vti);

        Integer existing = map.get(key);
        if (existing != null) return existing;

        int newIndex = posOut.size();
        map.put(key, newIndex);

        posOut.add(posSrc.get(vi));
        uvOut.add(vti >= 0 ? uvSrc.get(vti) : new Vec2(0.0, 0.0));

        // нормали всё равно пересчитаем, но чтобы размеры сошлись:
        nrmOut.add(vni >= 0 ? nrmSrc.get(vni) : new Vec3(0.0, 1.0, 0.0));

        return newIndex;
    }

    /** OBJ индексы бывают 1-based, а ещё бывают отрицательные (от конца). */
    private static int parseObjIndex(String s, int size) {
        int idx = Integer.parseInt(s);
        if (idx > 0) return idx - 1;
        // отрицательный: -1 означает последний
        return size + idx;
    }

    private static final class VertexKey {
        final int v;
        final int vt;

        VertexKey(int v, int vt) { this.v = v; this.vt = vt; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VertexKey)) return false;
            VertexKey k = (VertexKey) o;
            return v == k.v && vt == k.vt;
        }
        @Override public int hashCode() {
            return 31 * v + vt;
        }
    }
}
