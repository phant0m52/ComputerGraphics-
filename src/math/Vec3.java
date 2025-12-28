package math;

import java.util.Objects;

public final class Vec3 {
    public final double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public static Vec3 of(double x, double y, double z) { return new Vec3(x, y, z); }

    public Vec3 add(Vec3 o) { return new Vec3(x + o.x, y + o.y, z + o.z); }
    public Vec3 sub(Vec3 o) { return new Vec3(x - o.x, y - o.y, z - o.z); }
    public Vec3 mul(double s) { return new Vec3(x * s, y * s, z * s); }
    public Vec3 div(double s) { return new Vec3(x / s, y / s, z / s); }

    public double dot(Vec3 o) { return x * o.x + y * o.y + z * o.z; }

    public Vec3 cross(Vec3 o) {
        return new Vec3(
                y * o.z - z * o.y,
                z * o.x - x * o.z,
                x * o.y - y * o.x
        );
    }

    public double length() { return Math.sqrt(dot(this)); }

    public Vec3 normalized() {
        double len = length();
        if (len == 0.0) return new Vec3(0, 0, 0);
        return div(len);
    }

    @Override public String toString() { return "Vec3(" + x + ", " + y + ", " + z + ")"; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Vec3 v)) return false;
        return Double.compare(x, v.x) == 0 && Double.compare(y, v.y) == 0 && Double.compare(z, v.z) == 0;
    }
    @Override public int hashCode() { return Objects.hash(x, y, z); }
}
