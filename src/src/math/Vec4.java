package math;

public final class Vec4 {
    public static final Vec4 ZERO = new Vec4(0.0, 0.0, 0.0, 0.0);

    public final double x;
    public final double y;
    public final double z;
    public final double w;

    public Vec4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Vec4 zero() {
        return ZERO;
    }

    public static Vec4 point(Vec3 v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        return new Vec4(v.x, v.y, v.z, 1.0);
    }

    public static Vec4 direction(Vec3 v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        return new Vec4(v.x, v.y, v.z, 0.0);
    }

    public Vec4 add(Vec4 b) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        return new Vec4(this.x + b.x, this.y + b.y, this.z + b.z, this.w + b.w);
    }

    public Vec4 sub(Vec4 b) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        return new Vec4(this.x - b.x, this.y - b.y, this.z - b.z, this.w - b.w);
    }

    public Vec4 scale(double k) {
        return new Vec4(this.x * k, this.y * k, this.z * k, this.w * k);
    }

    public double dot(Vec4 b) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        return this.x * b.x + this.y * b.y + this.z * b.z + this.w * b.w;
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vec4 normalized() {
        double len = length();
        if (len <= MathUtil.EPS) {
            return Vec4.ZERO;
        }
        return new Vec4(this.x / len, this.y / len, this.z / len, this.w / len);
    }

    public double distanceSquaredTo(Vec4 b) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        double dx = this.x - b.x;
        double dy = this.y - b.y;
        double dz = this.z - b.z;
        double dw = this.w - b.w;
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    public double distanceTo(Vec4 b) {
        return Math.sqrt(distanceSquaredTo(b));
    }

    public Vec4 linearInterpolation(Vec4 b, double t) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        if (t < 0.0) t = 0.0;
        else if (t > 1.0) t = 1.0;

        double nx = this.x + (b.x - this.x) * t;
        double ny = this.y + (b.y - this.y) * t;
        double nz = this.z + (b.z - this.z) * t;
        double nw = this.w + (b.w - this.w) * t;
        return new Vec4(nx, ny, nz, nw);
    }

    public boolean epsEquals(Vec4 b, double eps) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        if (eps < 0.0) {
            throw new IllegalArgumentException("eps must be >= 0");
        }
        return Math.abs(this.x - b.x) <= eps
                && Math.abs(this.y - b.y) <= eps
                && Math.abs(this.z - b.z) <= eps
                && Math.abs(this.w - b.w) <= eps;
    }

    public Vec3 toVec3() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "Vec4(" + x + ", " + y + ", " + z + ", " + w + ")";
    }
}
