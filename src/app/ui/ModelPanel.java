package app.ui;

import engine.*;
import math.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Панель для просмотра 3D:
 * - управление активной камерой WASD + мышь
 * - софтверная растеризация (z-buffer, текстура, освещение)
 */
public final class ModelPanel extends JPanel {

    private final SwingInput input = new SwingInput();

    private final Scene scene = new Scene();
    private final CameraController controller = new CameraController(scene.getActiveCamera());

    private final RenderSettings renderSettings = new RenderSettings();

    private ModelInstance instance;

    private long lastNs = 0;

    public ModelPanel() {
        setPreferredSize(new Dimension(520, 560));
        setBackground(new Color(40, 40, 40));

        // стартовая камера: чуть отъезжаем и смотрим на (0,0,0)
        Camera cam0 = scene.getActiveCamera();
        cam0.setPosition(new Vec3(0, 0, 4));
        cam0.setYaw(Math.PI);
        cam0.setPitch(0);

        input.attachTo(this);
        setFocusable(true);

        // ~60 fps
        new Timer(16, e -> tick()).start();
    }

    public Scene getScene() {
        return scene;
    }

    public RenderSettings getRenderSettings() {
        return renderSettings;
    }

    public void setInstance(ModelInstance inst) {
        this.instance = inst;
        repaint();
        requestFocusInWindow();
    }

    public ModelInstance getInstance() {
        return instance;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    private void tick() {
        long now = System.nanoTime();
        if (lastNs == 0) lastNs = now;
        double dt = (now - lastNs) / 1_000_000_000.0;
        lastNs = now;

        // controller должен управлять активной камерой
        Camera active = scene.getActiveCamera();
        if (active != null && controller.getCamera() != active) controller.setCamera(active);

        controller.update(dt, input);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);

        int w = Math.max(2, getWidth());
        int h = Math.max(2, getHeight());

        Camera cam = scene.getActiveCamera();

        List<ModelInstance> cameraIcons = buildCameraIcons();

        BufferedImage img = SoftwareRenderer.render(instance, cameraIcons, cam, renderSettings, w, h);

        gg.drawImage(img, 0, 0, null);

        // маленький HUD
        if (cam != null) {
            gg.setColor(new Color(255, 255, 255, 180));
            gg.setFont(gg.getFont().deriveFont(12f));
            gg.drawString("Camera #" + (scene.getActiveIndex() + 1) +
                            "  pos=" + fmt(cam.getPosition()) +
                            "  yaw=" + String.format("%.1f°", Math.toDegrees(cam.getYaw())) +
                            "  pitch=" + String.format("%.1f°", Math.toDegrees(cam.getPitch())),
                    10, 18);
        }
    }

    private List<ModelInstance> buildCameraIcons() {
        List<ModelInstance> r = new ArrayList<>();
        if (scene.getCameras().size() <= 1) return r;

        // маленькая "пирамидка" как иконка камеры
        Mesh camMesh = CameraIconMesh.get();

        for (int i = 0; i < scene.getCameras().size(); i++) {
            if (i == scene.getActiveIndex()) continue;
            Camera c = scene.getCameras().get(i);

            Transform t = new Transform();
            t.setPosition(c.getPosition());

            // повернём иконку по yaw/pitch (приблизительно)
            t.setRotation(new Vec3(c.getPitch(), c.getYaw(), 0.0));

            // масштаб
            t.setScale(new Vec3(0.2, 0.2, 0.2));

            r.add(new ModelInstance(camMesh, t));
        }
        return r;
    }

    private static String fmt(Vec3 v) {
        return String.format("(%.2f, %.2f, %.2f)", v.x, v.y, v.z);
    }

    /** Вынесено в отдельный класс, чтобы не создавать mesh каждый кадр. */
    private static final class CameraIconMesh {
        private static Mesh cached;

        static Mesh get() {
            if (cached != null) return cached;

            // Пирамида: 5 вершин, 6 треугольников (основание можно не рисовать)
            List<Vec3> p = List.of(
                    new Vec3(0, 0, 0),      // 0: tip
                    new Vec3(-1, -0.6, -1), // 1
                    new Vec3( 1, -0.6, -1), // 2
                    new Vec3( 1,  0.6, -1), // 3
                    new Vec3(-1,  0.6, -1)  // 4
            );

            // uv и нормали заполним дефолтом, дальше пересчитаем
            int[] idx = new int[] {
                    0, 1, 2,
                    0, 2, 3,
                    0, 3, 4,
                    0, 4, 1,
                    1, 4, 3,
                    1, 3, 2
            };
            cached = new Mesh(p, idx).recalculateNormals();
            return cached;
        }
    }
}
