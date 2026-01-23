package engine;

import math.Mat4;
import math.Vec2;
import math.Vec3;
import math.Vec4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mesh = геометрия в локальных координатах (без Transform).
 *
 * positions: список вершин (Vec3) в model space
 * texCoords: uv-координаты (Vec2) для каждой вершины (0..1). Может быть null/пусто.
 * normals: нормали (Vec3) для каждой вершины. Может быть null/пусто, но в проекте мы их пересчитываем.
 *
 * indices: индексы треугольников (каждые 3 числа = один треугольник)
 */
public final class Mesh {

    private final List<Vec3> positions;
    private final List<Vec2> texCoords;
    private final List<Vec3> normals;
    private final int[] indices;

    /** Минимальный конструктор: только позиции и индексы (uv=0,0, normals=0,1,0). */
    public Mesh(List<Vec3> positions, int[] indices) {
        this(positions, createDefaultUVs(positions), createDefaultNormals(positions), indices);
    }

    public Mesh(List<Vec3> positions, List<Vec2> texCoords, List<Vec3> normals, int[] indices) {
        if (positions == null) throw new NullPointerException("positions must not be null");
        if (indices == null) throw new NullPointerException("indices must not be null");
        if (positions.isEmpty()) throw new IllegalArgumentException("positions must not be empty");
        if (indices.length % 3 != 0) throw new IllegalArgumentException("indices length must be multiple of 3");

        if (texCoords == null) throw new NullPointerException("texCoords must not be null");
        if (normals == null) throw new NullPointerException("normals must not be null");
        if (texCoords.size() != positions.size())
            throw new IllegalArgumentException("texCoords size must equal positions size");
        if (normals.size() != positions.size())
            throw new IllegalArgumentException("normals size must equal positions size");

        this.positions = Collections.unmodifiableList(new ArrayList<>(positions));
        this.texCoords = Collections.unmodifiableList(new ArrayList<>(texCoords));
        this.normals = Collections.unmodifiableList(new ArrayList<>(normals));
        this.indices = indices.clone();

        int n = this.positions.size();
        for (int idx : this.indices) {
            if (idx < 0 || idx >= n) {
                throw new IllegalArgumentException("index out of range: " + idx + " (vertices=" + n + ")");
            }
        }
    }

    private static List<Vec2> createDefaultUVs(List<Vec3> positions) {
        List<Vec2> uv = new ArrayList<>(positions.size());
        for (int i = 0; i < positions.size(); i++) uv.add(new Vec2(0.0, 0.0));
        return uv;
    }

    private static List<Vec3> createDefaultNormals(List<Vec3> positions) {
        List<Vec3> n = new ArrayList<>(positions.size());
        for (int i = 0; i < positions.size(); i++) n.add(new Vec3(0.0, 1.0, 0.0));
        return n;
    }

    public List<Vec3> getVertices() { // для совместимости со старым кодом
        return positions;
    }

    public List<Vec3> getPositions() { return positions; }
    public List<Vec2> getTexCoords() { return texCoords; }
    public List<Vec3> getNormals() { return normals; }

    public int[] getIndices() {
        return indices.clone();
    }

    public int triangleCount() {
        return indices.length / 3;
    }

    public int vertexCount() {
        return positions.size();
    }

    /**
     * Возвращает новый Mesh, где позиции вершин преобразованы матрицей transform.
     * uv и нормали при этом сохраняются (нормали можно пересчитать отдельно).
     *
     * Конвенция: column-vector, т.е. v' = M * v.
     */
    public Mesh transformed(Mat4 transform) {
        if (transform == null) throw new NullPointerException("transform must not be null");

        List<Vec3> outPos = new ArrayList<>(positions.size());
        for (Vec3 v : positions) {
            Vec4 p = Vec4.point(v);        // w=1
            Vec4 tp = transform.multiply(p);

            double w = tp.w;
            if (Math.abs(w) > 1e-12) {
                outPos.add(new Vec3(tp.x / w, tp.y / w, tp.z / w));
            } else {
                outPos.add(new Vec3(tp.x, tp.y, tp.z));
            }
        }

        // Нормали при bake'е трансформации правильнее пересчитать, но
        // на нашем уровне можно оставить как есть.
        return new Mesh(outPos, texCoords, normals, indices);
    }

    /** Пересчитать нормали (сглаженные): суммируем нормали треугольников к вершинам и нормализуем. */
    public Mesh recalculateNormals() {
        int n = positions.size();
        Vec3[] acc = new Vec3[n];
        for (int i = 0; i < n; i++) acc[i] = new Vec3(0, 0, 0);

        for (int i = 0; i < indices.length; i += 3) {
            int ia = indices[i], ib = indices[i + 1], ic = indices[i + 2];
            Vec3 a = positions.get(ia);
            Vec3 b = positions.get(ib);
            Vec3 c = positions.get(ic);

            Vec3 e1 = b.sub(a);
            Vec3 e2 = c.sub(a);
            Vec3 fn = e1.cross(e2); // не нормализуем, чтобы вес по площади был
            acc[ia] = acc[ia].add(fn);
            acc[ib] = acc[ib].add(fn);
            acc[ic] = acc[ic].add(fn);
        }

        List<Vec3> outN = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Vec3 nn = acc[i];
            double len = nn.length();
            if (len < 1e-12) outN.add(new Vec3(0, 1, 0));
            else outN.add(nn.scale(1.0 / len));
        }
        return new Mesh(positions, texCoords, outN, indices);
    }
}
