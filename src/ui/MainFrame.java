package ui;

import engine.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public final class MainFrame extends JFrame {
    private final Camera camera = new Camera();
    private final RenderPanel renderPanel = new RenderPanel(camera, Mesh.cube());
    private final TransformPanel transformPanel = new TransformPanel(renderPanel);

    private final FpsCameraController controller;

    public MainFrame() {
        super("Mini3D (column-vectors, own math)");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(renderPanel, BorderLayout.CENTER);
        add(transformPanel, BorderLayout.EAST);

        setJMenuBar(buildMenu());

        setSize(1200, 800);
        setLocationRelativeTo(null);

        controller = new FpsCameraController(camera, renderPanel);

        // Таймер обновления контроллера (движение камеры)
        final long[] last = {System.nanoTime()};
        Timer t = new Timer(16, e -> {
            long now = System.nanoTime();
            double dt = (now - last[0]) / 1_000_000_000.0;
            last[0] = now;

            controller.update(dt);
            renderPanel.repaint();
        });
        t.start();
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");

        JMenuItem open = new JMenuItem("Open OBJ...");
        open.addActionListener(e -> onOpen());

        JMenuItem saveRaw = new JMenuItem("Save OBJ (original vertices)...");
        saveRaw.addActionListener(e -> onSave(false));

        JMenuItem saveApplied = new JMenuItem("Save OBJ (with model transform applied)...");
        saveApplied.addActionListener(e -> onSave(true));

        JMenuItem resetToCube = new JMenuItem("Reset to Cube");
        resetToCube.addActionListener(e -> renderPanel.setMesh(Mesh.cube()));

        file.add(open);
        file.add(saveRaw);
        file.add(saveApplied);
        file.addSeparator();
        file.add(resetToCube);

        bar.add(file);
        return bar;
    }

    private void onOpen() {
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Open OBJ");
        if (ch.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = ch.getSelectedFile();
        try {
            Mesh mesh = ObjIO.load(f);
            renderPanel.setMesh(mesh);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Open error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave(boolean applyTransform) {
        Mesh mesh = renderPanel.getMesh();
        if (mesh == null) return;

        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle(applyTransform ? "Save OBJ (applied)" : "Save OBJ (original)");
        if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = ch.getSelectedFile();
        try {
            ObjIO.save(f, mesh, applyTransform);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
