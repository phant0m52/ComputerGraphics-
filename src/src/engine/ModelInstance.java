package engine;

import math.Mat4;
import math.Vec3;
import math.Vec4;

import java.util.ArrayList;
import java.util.List;

/**
 * ModelInstance = экземпляр меша в сцене (меш + Transform).
 * Один Mesh можно использовать много раз с разными Transform.
 */
public final class ModelInstance {
    private final Mesh mesh;
    private final Transform transform;

    public ModelInstance(Mesh mesh) {
        this(mesh, new Transform());
    }

    public ModelInstance(Mesh mesh, Transform transform) {
        if (mesh == null) throw new NullPointerException("mesh must not be null");
        if (transform == null) throw new NullPointerException("transform must not be null");
        this.mesh = mesh;
        this.transform = transform;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Transform getTransform() {
        return transform;
    }

    /**
     * Возвращает новый ModelInstance, где текущий Transform "запечён" в геометрию.
     * То есть vertices становятся уже в world (относительно текущего instance),
     * а Transform сбрасывается в identity.
     */
    public ModelInstance baked() {
        Mesh bakedMesh = bakedMesh();
        return new ModelInstance(bakedMesh, new Transform());
    }

    /**
     * Возвращает Mesh с применённым Transform (bake), без создания нового instance.
     */
    public Mesh bakedMesh() {
        Mat4 m = transform.toMatrix();
        return mesh.transformed(m);
    }

    /**
     * Преобразует вершины меша из local в world по текущему Transform.
     * Это как раз ваш шаг local -> world.
     */
    public List<Vec3> getWorldVertices() {
        Mat4 m = transform.toMatrix();

        List<Vec3> out = new ArrayList<>(mesh.vertexCount());
        for (Vec3 v : mesh.getVertices()) {
            Vec4 p = Vec4.point(v);       // w=1, значит перенос будет работать
            Vec4 pw = m.multiply(p);      // column-vector: world = M * local
            out.add(new Vec3(pw.x, pw.y, pw.z));
        }
        return out;
    }
}
