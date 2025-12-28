package engine;

import math.Mat4;
import math.Vec3;
import math.Vec4;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Renderer {

    public void render(Graphics2D g, int w, int h, Mesh mesh, Camera camera) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Mat4 model = mesh.transform.toMatrix();
        Mat4 view = camera.viewMatrix();
        Mat4 proj = camera.projectionMatrix();

        // Конвейер под столбцы: clip = P * V * M * p
        Mat4 mvp = proj.mul(view).mul(model);

        // Проецируем все вершины (сохраним экранные координаты)
        List<Point> screen = new ArrayList<>(mesh.vertices.size());
        List<Boolean> valid = new ArrayList<>(mesh.vertices.size());

        for (Vec3 v : mesh.vertices) {
            Vec4 clip = mvp.mul(Vec4.point(v));

            // простая отбраковка за камерой (w <= 0)
            if (clip.w <= 1e-9) {
                screen.add(new Point(0, 0));
                valid.add(false);
                continue;
            }

            double ndcX = clip.x / clip.w;
            double ndcY = clip.y / clip.w;
            double ndcZ = clip.z / clip.w;

            // грубая NDC-отбраковка (можно оставить)
            if (ndcZ < -1.5 || ndcZ > 1.5) {
                screen.add(new Point(0, 0));
                valid.add(false);
                continue;
            }

            int sx = (int) Math.round((ndcX * 0.5 + 0.5) * (w - 1));
            int sy = (int) Math.round(((-ndcY) * 0.5 + 0.5) * (h - 1)); // y вниз
            screen.add(new Point(sx, sy));
            valid.add(true);
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        g.setColor(Color.WHITE);
        for (int[] tri : mesh.faces) {
            int a = tri[0], b = tri[1], c = tri[2];
            if (!valid.get(a) || !valid.get(b) || !valid.get(c)) continue;

            Point pa = screen.get(a);
            Point pb = screen.get(b);
            Point pc = screen.get(c);

            g.drawLine(pa.x, pa.y, pb.x, pb.y);
            g.drawLine(pb.x, pb.y, pc.x, pc.y);
            g.drawLine(pc.x, pc.y, pa.x, pa.y);
        }

        // небольшой HUD
        g.setColor(new Color(255,255,255,180));
        g.drawString("RMB + mouse: look | WASD: move | wheel: speed | SHIFT: boost", 10, 20);
    }
}
