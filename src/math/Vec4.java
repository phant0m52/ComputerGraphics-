package math;

import java.util.Objects;

public final class Vec4 {
    public final double x, y, z, w;

    public Vec4(double x, double y, double z, double w) {
        this.x = x; this.y = y; this.z = z; this.w = w;
    }

    public static Vec4 point(Vec3 p) { return new Vec4(p.x, p.y, p.z, 1.0); }
    public static Vec4 dir(Vec3 d)   { return new Vec4(d.x, d.y, d.z, 0.0); }

    public Vec3 xyz() { return new Vec3(x, y, z); }

    @Override public String toString() { return "Vec4(" + x + ", " + y + ", " + z + ", " + w + ")"; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Vec4 v)) return false;
        return Double.compare(x, v.x) == 0 && Double.compare(y, v.y) == 0 &&
                Double.compare(z, v.z) == 0 && Double.compare(w, v.w) == 0;
    }
    @Override public int hashCode() { return Objects.hash(x, y, z, w); }
}
