package engine;

import math.Mat4;
import math.Vec3;
import math.Vec4;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer (пока очень простой) — показывает, где происходит конвейер.
 * Сейчас делает только local -> world через Transform.
 * Позже сюда добавятся camera/view/projection/viewport и растеризация.
 */
public final class Renderer {

    /**
     * Возвращает вершины модели в мировых координатах.
     * Это шаг local -> world: world = M_model * local
     */
    public List<Vec3> renderWorld(ModelInstance instance) {
        if (instance == null) throw new NullPointerException("instance must not be null");

        Mesh mesh = instance.getMesh();
        Transform tr = instance.getTransform();

        Mat4 model = tr.toMatrix();

        List<Vec3> worldVerts = new ArrayList<>(mesh.vertexCount());
        for (Vec3 vLocal : mesh.getVertices()) {
            Vec4 p = Vec4.point(vLocal);     // точка (w=1)
            Vec4 pw = model.multiply(p);     // world = M * local
            worldVerts.add(new Vec3(pw.x, pw.y, pw.z));
        }
        return worldVerts;
    }

    /**
     * Если тебе удобнее получать сразу треугольники (по индексам),
     * можно вернуть список треугольников как троек Vec3.
     * Здесь оставляю как заготовку: используешь indices из mesh.
     */
    public List<Vec3[]> renderWorldTriangles(ModelInstance instance) {
        if (instance == null) throw new NullPointerException("instance must not be null");

        List<Vec3> w = renderWorld(instance);
        int[] idx = instance.getMesh().getIndices();

        List<Vec3[]> tris = new ArrayList<>(idx.length / 3);
        for (int i = 0; i < idx.length; i += 3) {
            tris.add(new Vec3[] {
                    w.get(idx[i]),
                    w.get(idx[i + 1]),
                    w.get(idx[i + 2])
            });
        }
        return tris;
    }
}
