package app.ui;

import engine.InputState;

import javax.swing.*;
import java.awt.event.*;
import java.util.BitSet;

public final class SwingInput implements InputState {

    private final BitSet keys = new BitSet(512);

    private boolean hasLastMouse;
    private int lastX, lastY;
    private double dx, dy;
    private double wheel;

    private boolean rotateOnlyWhenRightButtonDown = true;
    private boolean rightDown = false;

    public void attachTo(JComponent c) {
        c.setFocusable(true);

        c.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) rightDown = true;
                c.requestFocusInWindow();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) rightDown = false;
            }
        });

        c.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { handleMove(e.getX(), e.getY()); }
            @Override public void mouseDragged(MouseEvent e) { handleMove(e.getX(), e.getY()); }
        });

        c.addMouseWheelListener(e -> wheel += e.getPreciseWheelRotation());

        c.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { keys.set(e.getKeyCode(), true); }
            @Override public void keyReleased(KeyEvent e) { keys.set(e.getKeyCode(), false); }
        });
    }

    private void handleMove(int x, int y) {
        if (!hasLastMouse) {
            hasLastMouse = true;
            lastX = x; lastY = y;
            return;
        }
        int mx = x - lastX;
        int my = y - lastY;
        lastX = x; lastY = y;

        if (!rotateOnlyWhenRightButtonDown || rightDown) {
            dx += mx;
            dy += my;
        }
    }

    @Override public boolean isKeyDown(int keyCode) { return keys.get(keyCode); }

    @Override public double consumeMouseDeltaX() { double v = dx; dx = 0; return v; }
    @Override public double consumeMouseDeltaY() { double v = dy; dy = 0; return v; }
    @Override public double consumeMouseWheelDelta() { double v = wheel; wheel = 0; return v; }
}
