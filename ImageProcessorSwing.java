package app.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessorSwing {

    public static BufferedImage apply(
            BufferedImage input,
            boolean grayscale,
            boolean edges,
            float brightness,
            float contrast
    ) {
        int w = input.getWidth();
        int h = input.getHeight();

        BufferedImage base = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = input.getRGB(x, y);
                Color c = new Color(argb, true);

                float r = c.getRed() / 255f;
                float g = c.getGreen() / 255f;
                float b = c.getBlue() / 255f;
                float a = c.getAlpha() / 255f;

                if (grayscale) {
                    float lum = (r + g + b) / 3f;
                    r = g = b = lum;
                }

                r = (r - 0.5f) * contrast + 0.5f + brightness;
                g = (g - 0.5f) * contrast + 0.5f + brightness;
                b = (b - 0.5f) * contrast + 0.5f + brightness;

                r = clamp01(r);
                g = clamp01(g);
                b = clamp01(b);

                int out = new Color(r, g, b, a).getRGB();
                base.setRGB(x, y, out);
            }
        }

        if (!edges) return base;

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                float gx = luminance(base.getRGB(x + 1, y)) - luminance(base.getRGB(x - 1, y));
                float gy = luminance(base.getRGB(x, y + 1)) - luminance(base.getRGB(x, y - 1));
                float mag = clamp01((float) Math.sqrt(gx * gx + gy * gy));
                int v = (int) (mag * 255);
                out.setRGB(x, y, new Color(v, v, v, 255).getRGB());
            }
        }

        for (int x = 0; x < w; x++) {
            out.setRGB(x, 0, Color.BLACK.getRGB());
            out.setRGB(x, h - 1, Color.BLACK.getRGB());
        }
        for (int y = 0; y < h; y++) {
            out.setRGB(0, y, Color.BLACK.getRGB());
            out.setRGB(w - 1, y, Color.BLACK.getRGB());
        }

        return out;
    }

    private static float luminance(int argb) {
        Color c = new Color(argb, true);
        float r = c.getRed() / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue() / 255f;
        return 0.2126f * r + 0.7152f * g + 0.0722f * b;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
