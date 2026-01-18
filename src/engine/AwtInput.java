package engine;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.BitSet;

/**
 * AWT/Swing адаптер ввода: клавиатура + накопленные дельты мыши.
 *
 * Подключение:
 *   AwtInput input = new AwtInput();
 *   input.attachTo(frameOrCanvas);
 *
 * В игровом цикле:
 *   controller.update(dtSeconds, input);
 */
public final class AwtInput implements InputState, KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {

    private final BitSet keys = new BitSet(512);

    private boolean hasLastMouse;
    private int lastMouseX;
    private int lastMouseY;

    private double mouseDx;
    private double mouseDy;
    private double wheel;

    /** Если true — мышь учитывается только при зажатой правой кнопке. */
    private boolean rotateOnlyWhenRightButtonDown = true;
    private boolean rightButtonDown;

    public void attachTo(Component c) {
        if (c == null) throw new NullPointerException("component must not be null");
        c.addKeyListener(this);
        c.addMouseMotionListener(this);
        c.addMouseListener(this);
        c.addMouseWheelListener(this);
        c.setFocusable(true);
        c.requestFocusInWindow();
    }

    public void setRotateOnlyWhenRightButtonDown(boolean value) {
        this.rotateOnlyWhenRightButtonDown = value;
    }

    @Override
    public boolean isKeyDown(int keyCode) {
        if (keyCode < 0) return false;
        return keys.get(keyCode);
    }

    @Override
    public double consumeMouseDeltaX() {
        double v = mouseDx;
        mouseDx = 0.0;
        return v;
    }

    @Override
    public double consumeMouseDeltaY() {
        double v = mouseDy;
        mouseDy = 0.0;
        return v;
    }

    @Override
    public double consumeMouseWheelDelta() {
        double v = wheel;
        wheel = 0.0;
        return v;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e == null) return;
        keys.set(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e == null) return;
        keys.set(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) {
        handleMouseMove(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        handleMouseMove(e);
    }

    private void handleMouseMove(MouseEvent e) {
        if (e == null) return;
        int x = e.getX();
        int y = e.getY();

        if (!hasLastMouse) {
            hasLastMouse = true;
            lastMouseX = x;
            lastMouseY = y;
            return;
        }

        int dx = x - lastMouseX;
        int dy = y - lastMouseY;
        lastMouseX = x;
        lastMouseY = y;

        if (!rotateOnlyWhenRightButtonDown || rightButtonDown) {
            mouseDx += dx;
            mouseDy += dy;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e == null) return;
        wheel += e.getPreciseWheelRotation();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e == null) return;
        if (e.getButton() == MouseEvent.BUTTON3) rightButtonDown = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e == null) return;
        if (e.getButton() == MouseEvent.BUTTON3) rightButtonDown = false;
    }

    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}
