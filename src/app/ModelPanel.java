package app.ui;

import engine.Camera;
import engine.CameraController;
import engine.ModelInstance;
import math.Mat4;
import math.Vec4;

import javax.swing.*;
import java.awt.*;

public final class ModelPanel extends JPanel {

    private final SwingInput input = new SwingInput();

    // yaw=PI — смотрим в сторону -Z, то есть на модель около (0,0,0)
    private final Camera camera = new Camera(new math.Vec3(0, 0, 4), Math.PI, 0);
    private final CameraController controller = new CameraController(camera);

    private ModelInstance instance;

    private long lastNs = 0;

    public ModelPanel() {
        setPreferredSize(new Dimension(520, 560));
        setBackground(new Color(40, 40, 40));

        input.attachTo(this);
        setFocusable(true);

        // ~60 fps
        new Timer(16, e -> tick()).start();
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

        controller.update(dt, input);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        if (instance == null) return;

        Graphics2D g = (Graphics2D) gg.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double w = Math.max(1.0, getWidth());
            double h = Math.max(1.0, getHeight());

            Mat4 model = instance.getTransform().toMatrix();
            Mat4 view = camera.getViewMatrix();
            Mat4 proj = Mat4.perspective(Math.toRadians(60), w / h, 0.1, 100.0);

            Mat4 mvp = proj.multiply(view).multiply(model);

            var verts = instance.getMesh().getVertices();
            double[] sx = new double[verts.size()];
            double[] sy = new double[verts.size()];
            boolean[] ok = new boolean[verts.size()];

            for (int i = 0; i < verts.size(); i++) {
                Vec4 p = Vec4.point(verts.get(i));
                Vec4 c = mvp.multiply(p);

                if (Math.abs(c.w) < 1e-12) { ok[i] = false; continue; }

                double ndcX = c.x / c.w;
                double ndcY = c.y / c.w;
                double ndcZ = c.z / c.w;

                if (ndcZ < -1 || ndcZ > 1) { ok[i] = false; continue; }

                sx[i] = (ndcX + 1.0) * 0.5 * w;
                sy[i] = (1.0 - (ndcY + 1.0) * 0.5) * h;
                ok[i] = true;
            }

            int[] idx = instance.getMesh().getIndices();

            g.setColor(Color.WHITE);
            for (int i = 0; i < idx.length; i += 3) {
                int a = idx[i], b = idx[i + 1], c = idx[i + 2];
                if (ok[a] && ok[b]) g.drawLine((int)sx[a], (int)sy[a], (int)sx[b], (int)sy[b]);
                if (ok[b] && ok[c]) g.drawLine((int)sx[b], (int)sy[b], (int)sx[c], (int)sy[c]);
                if (ok[c] && ok[a]) g.drawLine((int)sx[c], (int)sy[c], (int)sx[a], (int)sy[a]);
            }

        } finally {
            g.dispose();
        }
    }
}
