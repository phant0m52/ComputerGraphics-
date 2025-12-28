package ui;

import engine.Camera;
import engine.Mesh;
import engine.Renderer;

import javax.swing.*;
import java.awt.*;

public final class RenderPanel extends JPanel {
    private final Renderer renderer = new Renderer();
    private final Camera camera;
    private Mesh mesh;

    public RenderPanel(Camera camera, Mesh mesh) {
        this.camera = camera;
        this.mesh = mesh;
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        repaint();
    }

    public Mesh getMesh() { return mesh; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        camera.setAspect(getWidth() / (double) Math.max(1, getHeight()));
        renderer.render((Graphics2D) g, getWidth(), getHeight(), mesh, camera);
    }
}
