package app.image;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class ImageProcessor {

    public static WritableImage apply(
            Image input,
            boolean grayscale,
            boolean edges,
            double brightness,
            double contrast
    ) {
        int w = (int) input.getWidth();
        int h = (int) input.getHeight();
        PixelReader pr = input.getPixelReader();
        WritableImage out = new WritableImage(w, h);
        PixelWriter pw = out.getPixelWriter();

        Color[][] buf = new Color[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = pr.getColor(x, y);

                if (grayscale) {
                    double g = (c.getRed() + c.getGreen() + c.getBlue()) / 3.0;
                    c = new Color(g, g, g, c.getOpacity());
                }

                c = adjust(c, brightness, contrast);
                buf[x][y] = c;
            }
        }

        if (!edges) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    pw.setColor(x, y, buf[x][y]);
                }
            }
            return out;
        }

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                double gx = lum(buf[x + 1][y]) - lum(buf[x - 1][y]);
                double gy = lum(buf[x][y + 1]) - lum(buf[x][y - 1]);
                double mag = clamp(Math.sqrt(gx * gx + gy * gy));
                pw.setColor(x, y, new Color(mag, mag, mag, 1));
            }
        }

        for (int x=0; x<w; x++) { pw.setColor(x,0,Color.BLACK); pw.setColor(x,h-1,Color.BLACK); }
        for (int y=0; y<h; y++) { pw.setColor(0,y,Color.BLACK); pw.setColor(w-1,y,Color.BLACK); }

        return out;
    }

    private static Color adjust(Color c, double brightness, double contrast) {
        double r = ((c.getRed()   - 0.5) * contrast + 0.5) + brightness;
        double g = ((c.getGreen() - 0.5) * contrast + 0.5) + brightness;
        double b = ((c.getBlue()  - 0.5) * contrast + 0.5) + brightness;
        return new Color(clamp(r), clamp(g), clamp(b), c.getOpacity());
    }

    private static double lum(Color c) {
        return 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
