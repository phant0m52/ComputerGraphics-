package engine;

import math.Mat4;
import math.Vec2;
import math.Vec3;
import math.Vec4;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Простой софтверный рендерер:
 * - треугольники
 * - z-buffer
 * - режимы: базовый цвет / текстура / освещение / wireframe поверх
 *
 * Без отсечения/клиппинга и без супер-оптимизаций (уровень "2 курс").
 */
public final class SoftwareRenderer {

    private SoftwareRenderer() {}

    public static BufferedImage render(
            ModelInstance model,
            List<ModelInstance> extraInstances,
            Camera camera,
            RenderSettings settings,
            int width,
            int height
    ) {
        if (width <= 1 || height <= 1) throw new IllegalArgumentException("bad size");
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // zBuffer: меньше => ближе. Начинаем с +inf
        double[] z = new double[width * height];
        for (int i = 0; i < z.length; i++) z[i] = Double.POSITIVE_INFINITY;

        // background
        int bg = new Color(40, 40, 40).getRGB();
        int[] pixels = img.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < pixels.length; i++) pixels[i] = bg;

        if (camera == null) {
            img.setRGB(0, 0, width, height, pixels, 0, width);
            return img;
        }

        // матрицы
        Mat4 view = camera.getViewMatrix();
        Mat4 proj = Mat4.perspective(Math.toRadians(60), (double) width / (double) height, 0.1, 200.0);

        if (model != null) {
            drawInstance(model, camera, view, proj, settings, width, height, pixels, z);
        }

        if (extraInstances != null) {
            for (ModelInstance inst : extraInstances) {
                if (inst == null) continue;
                // для камер-иконок всегда wireframe
                RenderSettings s = new RenderSettings();
                s.drawWireframe = true;
                s.useLighting = false;
                s.useTexture = false;
                s.baseColor = new Color(255, 230, 120);
                drawInstance(inst, camera, view, proj, s, width, height, pixels, z);
            }
        }

        img.setRGB(0, 0, width, height, pixels, 0, width);
        return img;
    }

    private static void drawInstance(
            ModelInstance instance,
            Camera camera,
            Mat4 view,
            Mat4 proj,
            RenderSettings settings,
            int width,
            int height,
            int[] pixels,
            double[] zBuf
    ) {
        Mesh mesh = instance.getMesh();
        if (mesh == null) return;

        Mat4 model = instance.getTransform().toMatrix();
        Mat4 mvp = proj.multiply(view).multiply(model);

        List<Vec3> pos = mesh.getPositions();
        List<Vec2> uv = mesh.getTexCoords();
        List<Vec3> nrm = mesh.getNormals();
        int[] idx = mesh.getIndices();

        // предвычислим screen coords, depth (NDC z), world positions, world normals
        double[] sx = new double[pos.size()];
        double[] sy = new double[pos.size()];
        double[] sz = new double[pos.size()];
        Vec3[] worldPos = new Vec3[pos.size()];
        Vec3[] worldNrm = new Vec3[pos.size()];
        boolean[] ok = new boolean[pos.size()];

        Mat4 modelNoTrans = model; // нормали: на нашем уровне просто умножаем как direction
        for (int i = 0; i < pos.size(); i++) {
            Vec3 pLocal = pos.get(i);

            Vec4 pw = model.multiply(Vec4.point(pLocal));
            worldPos[i] = new Vec3(pw.x, pw.y, pw.z);

            Vec4 clip = mvp.multiply(Vec4.point(pLocal));
            if (Math.abs(clip.w) < 1e-12) { ok[i] = false; continue; }

            double ndcX = clip.x / clip.w;
            double ndcY = clip.y / clip.w;
            double ndcZ = clip.z / clip.w;

            // простой near/far check, без клиппинга
            if (ndcZ < -1.0 || ndcZ > 1.0) { ok[i] = false; continue; }

            sx[i] = (ndcX + 1.0) * 0.5 * width;
            sy[i] = (1.0 - (ndcY + 1.0) * 0.5) * height;

            // depth: [0..1] (меньше ближе)
            sz[i] = (ndcZ + 1.0) * 0.5;

            // нормаль
            Vec3 nLocal = nrm.get(i);
            Vec4 nw = modelNoTrans.multiply(Vec4.direction(nLocal));
            Vec3 nWorld = new Vec3(nw.x, nw.y, nw.z).normalized();
            worldNrm[i] = nWorld;

            ok[i] = true;
        }

        // РИСУЕМ ТРЕУГОЛЬНИКИ
        for (int t = 0; t < idx.length; t += 3) {
            int ia = idx[t], ib = idx[t + 1], ic = idx[t + 2];
            if (!(ok[ia] && ok[ib] && ok[ic])) continue;

            Vertex2D a = new Vertex2D(sx[ia], sy[ia], sz[ia], uv.get(ia), worldPos[ia], worldNrm[ia]);
            Vertex2D b = new Vertex2D(sx[ib], sy[ib], sz[ib], uv.get(ib), worldPos[ib], worldNrm[ib]);
            Vertex2D c = new Vertex2D(sx[ic], sy[ic], sz[ic], uv.get(ic), worldPos[ic], worldNrm[ic]);

            // backface culling в screen-space (очень грубо, но быстро)
            double area2 = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
            if (area2 >= 0) continue;

            if (!settings.drawWireframe) {
                rasterTriangle(a, b, c, camera, settings, width, height, pixels, zBuf);
            } else {
                // если wireframe включен, мы всё равно должны заливать (если другие флаги не только wireframe).
                // Если нужно только wireframe — можно не заливать.
                boolean fill = settings.useLighting || settings.useTexture || settings.baseColor != null;
                if (!fill) {
                    drawLineZ(a, b, width, height, pixels, zBuf, settings.baseColor.getRGB());
                    drawLineZ(b, c, width, height, pixels, zBuf, settings.baseColor.getRGB());
                    drawLineZ(c, a, width, height, pixels, zBuf, settings.baseColor.getRGB());
                } else {
                    rasterTriangle(a, b, c, camera, settings, width, height, pixels, zBuf);
                }
            }
        }

        // wireframe поверх (с z-test)
        if (settings.drawWireframe) {
            int wire = Color.WHITE.getRGB();
            for (int t = 0; t < idx.length; t += 3) {
                int ia = idx[t], ib = idx[t + 1], ic = idx[t + 2];
                if (!(ok[ia] && ok[ib] && ok[ic])) continue;

                Vertex2D a = new Vertex2D(sx[ia], sy[ia], sz[ia], uv.get(ia), worldPos[ia], worldNrm[ia]);
                Vertex2D b = new Vertex2D(sx[ib], sy[ib], sz[ib], uv.get(ib), worldPos[ib], worldNrm[ib]);
                Vertex2D c = new Vertex2D(sx[ic], sy[ic], sz[ic], uv.get(ic), worldPos[ic], worldNrm[ic]);

                drawLineZ(a, b, width, height, pixels, zBuf, wire);
                drawLineZ(b, c, width, height, pixels, zBuf, wire);
                drawLineZ(c, a, width, height, pixels, zBuf, wire);
            }
        }
    }

    private static void rasterTriangle(
            Vertex2D v0, Vertex2D v1, Vertex2D v2,
            Camera camera,
            RenderSettings settings,
            int w, int h,
            int[] pixels,
            double[] zBuf
    ) {
        // bounding box
        int minX = (int) Math.floor(Math.min(v0.x, Math.min(v1.x, v2.x)));
        int maxX = (int) Math.ceil(Math.max(v0.x, Math.max(v1.x, v2.x)));
        int minY = (int) Math.floor(Math.min(v0.y, Math.min(v1.y, v2.y)));
        int maxY = (int) Math.ceil(Math.max(v0.y, Math.max(v1.y, v2.y)));

        if (minX < 0) minX = 0;
        if (minY < 0) minY = 0;
        if (maxX >= w) maxX = w - 1;
        if (maxY >= h) maxY = h - 1;

        double area = edge(v0.x, v0.y, v1.x, v1.y, v2.x, v2.y);
        if (Math.abs(area) < 1e-12) return;

        int baseRGB = settings.baseColor.getRGB();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                double px = x + 0.5;
                double py = y + 0.5;

                double w0 = edge(v1.x, v1.y, v2.x, v2.y, px, py);
                double w1 = edge(v2.x, v2.y, v0.x, v0.y, px, py);
                double w2 = edge(v0.x, v0.y, v1.x, v1.y, px, py);

                // inside (ориентированный треугольник)
                if (w0 < 0 || w1 < 0 || w2 < 0) continue;

                w0 /= area;
                w1 /= area;
                w2 /= area;

                // depth
                double z = v0.z * w0 + v1.z * w1 + v2.z * w2;
                int id = y * w + x;
                if (z >= zBuf[id]) continue;
                zBuf[id] = z;

                // color
                int rgb = baseRGB;

                Vec3 n = v0.nWorld.scale(w0).add(v1.nWorld.scale(w1)).add(v2.nWorld.scale(w2)).normalized();

                double intensity = 1.0;
                if (settings.useLighting) {
                    Vec3 p = v0.worldPos.scale(w0).add(v1.worldPos.scale(w1)).add(v2.worldPos.scale(w2));
                    Vec3 L = camera.getPosition().sub(p).normalized(); // свет в позиции камеры
                    double diff = Math.max(0.0, n.dot(L));
                    double ambient = 0.22;
                    intensity = ambient + (1.0 - ambient) * diff;
                }

                if (settings.useTexture && settings.texture != null) {
                    double u = v0.uv.x * w0 + v1.uv.x * w1 + v2.uv.x * w2;
                    double v = v0.uv.y * w0 + v1.uv.y * w1 + v2.uv.y * w2;
                    rgb = settings.texture.sample(u, v);
                }

                // modulate light (ARGB)
                if (settings.useLighting) {
                    int a = (rgb >>> 24) & 255;
                    int r = (rgb >>> 16) & 255;
                    int g = (rgb >>> 8) & 255;
                    int b = (rgb) & 255;
                    r = (int) Math.round(r * intensity);
                    g = (int) Math.round(g * intensity);
                    b = (int) Math.round(b * intensity);
                    r = clamp255(r); g = clamp255(g); b = clamp255(b);
                    rgb = (a << 24) | (r << 16) | (g << 8) | b;
                }

                pixels[id] = rgb;
            }
        }
    }

    private static double edge(double ax, double ay, double bx, double by, double px, double py) {
        return (px - ax) * (by - ay) - (py - ay) * (bx - ax);
    }

    /** Простая растеризация линии с Z-тестом (DDA). */
    private static void drawLineZ(Vertex2D a, Vertex2D b, int w, int h, int[] pixels, double[] zBuf, int rgb) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;
        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)));
        if (steps <= 0) return;

        double x = a.x;
        double y = a.y;
        double z = a.z;

        double sx = dx / steps;
        double sy = dy / steps;
        double sz = dz / steps;

        for (int i = 0; i <= steps; i++) {
            int ix = (int) Math.round(x);
            int iy = (int) Math.round(y);
            if (ix >= 0 && ix < w && iy >= 0 && iy < h) {
                int id = iy * w + ix;
                if (z < zBuf[id]) {
                    // не обновляем z, иначе линии могут "протыкать" заливку
                    pixels[id] = rgb;
                }
            }
            x += sx; y += sy; z += sz;
        }
    }

    private static int clamp255(int v) {
        return (v < 0) ? 0 : Math.min(v, 255);
    }

    private static final class Vertex2D {
        final double x, y, z;
        final Vec2 uv;
        final Vec3 worldPos;
        final Vec3 nWorld;

        Vertex2D(double x, double y, double z, Vec2 uv, Vec3 worldPos, Vec3 nWorld) {
            this.x = x; this.y = y; this.z = z;
            this.uv = uv;
            this.worldPos = worldPos;
            this.nWorld = nWorld;
        }
    }
}
