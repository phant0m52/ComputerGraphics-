package engine;

import java.awt.image.BufferedImage;

/**
 * Очень простой класс для выборки пикселей из текстуры.
 */
public final class Texture {
    private final BufferedImage img;
    private final int w;
    private final int h;

    public Texture(BufferedImage img) {
        if (img == null) throw new NullPointerException("img must not be null");
        this.img = img;
        this.w = img.getWidth();
        this.h = img.getHeight();
    }

    public int getWidth() { return w; }
    public int getHeight() { return h; }

    /**
     * Семплинг по UV (0..1). Тут clamp (обрезаем), без фильтрации (nearest).
     * Возвращает ARGB.
     */
    public int sample(double u, double v) {
        // OBJ обычно: v идёт снизу вверх, а в картинке сверху вниз => инверсия
        double uu = clamp(u, 0.0, 1.0);
        double vv = clamp(v, 0.0, 1.0);
        int x = (int) Math.round(uu * (w - 1));
        int y = (int) Math.round((1.0 - vv) * (h - 1));
        return img.getRGB(x, y);
    }

    private static double clamp(double x, double lo, double hi) {
        return (x < lo) ? lo : Math.min(x, hi);
    }
}
