package engine;

import math.Vec3;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public final class FpsCameraController implements MouseListener, MouseMotionListener, MouseWheelListener {
    private final Camera camera;
    private final JComponent input;

    private boolean rmbDown = false;
    private int lastX, lastY;

    private double mouseSensitivity = 0.005; // rad per px
    private double speed = 3.0;              // units/sec

    private final Set<Integer> keys = new HashSet<>();

    public FpsCameraController(Camera camera, JComponent input) {
        this.camera = camera;
        this.input = input;

        input.addMouseListener(this);
        input.addMouseMotionListener(this);
        input.addMouseWheelListener(this);

        setupKeyBindings(input);
    }

    private void setupKeyBindings(JComponent c) {
        int cond = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = c.getInputMap(cond);
        ActionMap am = c.getActionMap();

        bind(im, am, "pressed W", "pressed W", KeyEvent.VK_W, true);
        bind(im, am, "released W", "released W", KeyEvent.VK_W, false);
        bind(im, am, "pressed A", "pressed A", KeyEvent.VK_A, true);
        bind(im, am, "released A", "released A", KeyEvent.VK_A, false);
        bind(im, am, "pressed S", "pressed S", KeyEvent.VK_S, true);
        bind(im, am, "released S", "released S", KeyEvent.VK_S, false);
        bind(im, am, "pressed D", "pressed D", KeyEvent.VK_D, true);
        bind(im, am, "released D", "released D", KeyEvent.VK_D, false);

        bind(im, am, "pressed SHIFT", "pressed SHIFT", KeyEvent.VK_SHIFT, true);
        bind(im, am, "released SHIFT", "released SHIFT", KeyEvent.VK_SHIFT, false);
    }

    private void bind(InputMap im, ActionMap am, String name, String stroke, int key, boolean down) {
        im.put(KeyStroke.getKeyStroke(stroke), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (down) keys.add(key);
                else keys.remove(key);
            }
        });
    }

    public void update(double dt) {
        double curSpeed = keys.contains(KeyEvent.VK_SHIFT) ? speed * 3.0 : speed;

        Vec3 move = Vec3.of(0, 0, 0);
        if (keys.contains(KeyEvent.VK_W)) move = move.add(camera.forward());
        if (keys.contains(KeyEvent.VK_S)) move = move.sub(camera.forward());
        if (keys.contains(KeyEvent.VK_D)) move = move.add(camera.right());
        if (keys.contains(KeyEvent.VK_A)) move = move.sub(camera.right());

        if (move.length() > 0.0) {
            Vec3 newPos = camera.getPosition().add(move.normalized().mul(curSpeed * dt));
            camera.setPosition(newPos);
        }
    }

    @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            rmbDown = true;
            lastX = e.getX();
            lastY = e.getY();
        }
    }

    @Override public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) rmbDown = false;
    }

    @Override public void mouseDragged(MouseEvent e) {
        if (!rmbDown) return;
        int x = e.getX(), y = e.getY();
        int dx = x - lastX;
        int dy = y - lastY;
        lastX = x; lastY = y;

        camera.addYawPitch(dx * mouseSensitivity, dy * mouseSensitivity);
        input.repaint();
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        speed *= (notches > 0) ? 0.9 : 1.1;
        speed = Math.max(0.1, Math.min(100.0, speed));
    }

    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
