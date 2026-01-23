package math;

public final class MathUtil {


    public static double degToRad(double deg) { return deg * Math.PI / 180.0; }
    public static double radToDeg(double rad) { return rad * 180.0 / Math.PI; }

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
    public static final double EPS = 1e-9;
    private MathUtil() {}
}
