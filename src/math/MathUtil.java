package math;

public final class MathUtil {
    private MathUtil() {}

    public static double degToRad(double deg) { return deg * Math.PI / 180.0; }
    public static double radToDeg(double rad) { return rad * 180.0 / Math.PI; }

    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
