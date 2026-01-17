package engine;

import math.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mesh = чистая геометрия в локальных координатах (без Transform).
 * vertices: список вершин (Vec3) в local/model space
 * indices: индексы треугольников (каждые 3 числа = один треугольник)
 */
public final class Mesh {
    private final List<Vec3> vertices;
    private final int[] indices;

    public Mesh(List<Vec3> vertices, int[] indices) {
        if (vertices == null) throw new NullPointerException("vertices must not be null");
        if (indices == null) throw new NullPointerException("indices must not be null");
        if (vertices.isEmpty()) throw new IllegalArgumentException("vertices must not be empty");
        if (indices.length % 3 != 0) throw new IllegalArgumentException("indices length must be multiple of 3");

        // Вершины: копируем в новый список, чтобы извне не могли менять
        this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));

        // Индексы: копируем массив
        this.indices = indices.clone();

        // Проверка, что индексы в диапазоне
        int n = this.vertices.size();
        for (int idx : this.indices) {
            if (idx < 0 || idx >= n) {
                throw new IllegalArgumentException("index out of range: " + idx + " (vertices=" + n + ")");
            }
        }
    }

    public List<Vec3> getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices.clone();
    }

    public int triangleCount() {
        return indices.length / 3;
    }

    public int vertexCount() {
        return vertices.size();
    }
}
