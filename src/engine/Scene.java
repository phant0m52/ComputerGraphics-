package engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Сцена с несколькими камерами. */
public final class Scene {
    private final List<Camera> cameras = new ArrayList<>();
    private int activeIndex = 0;

    public Scene() {
        // по умолчанию одна камера
        cameras.add(new Camera());
        activeIndex = 0;
    }

    public List<Camera> getCameras() {
        return Collections.unmodifiableList(cameras);
    }

    public Camera getActiveCamera() {
        if (cameras.isEmpty()) return null;
        activeIndex = Math.max(0, Math.min(activeIndex, cameras.size() - 1));
        return cameras.get(activeIndex);
    }

    public int getActiveIndex() { return activeIndex; }

    public void setActiveIndex(int idx) {
        if (cameras.isEmpty()) { activeIndex = 0; return; }
        activeIndex = Math.max(0, Math.min(idx, cameras.size() - 1));
    }

    public void addCamera(Camera c) {
        if (c == null) throw new NullPointerException("camera must not be null");
        cameras.add(c);
        if (cameras.size() == 1) activeIndex = 0;
    }

    public void removeCamera(int idx) {
        if (cameras.isEmpty()) return;
        if (idx < 0 || idx >= cameras.size()) return;
        cameras.remove(idx);
        if (cameras.isEmpty()) {
            activeIndex = 0;
            return;
        }
        if (activeIndex >= cameras.size()) activeIndex = cameras.size() - 1;
    }
}
