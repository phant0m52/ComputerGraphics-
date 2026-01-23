package math;

public final class Vec2 {
    public static final Vec2 ZERO = new Vec2(0.0, 0.0);

    public final double x;
    public final double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vec2 zero() {
        return ZERO;
    }

    public Vec2 add(Vec2 b) {
        if (b == null) {
            throw new NullPointerException("b must nоt be null");
        }
        return new Vec2(this.x + b.x, this.y + b.y);
    }

    public Vec2 sub(Vec2 b) {
        if (b == null) {
            throw new NullPointerException("b must nоt be null");
        }
        return new Vec2(this.x - b.x, this.y - b.y);
    }

    public Vec2 scale(double k) {
        return new Vec2(this.x * k, this.y * k);
    }

    public double dot(Vec2 b) {
        if (b == null) {
            throw new NullPointerException(("b must not be null"));
        }
        return this.x * b.x + this.y * b.y;
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }


    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vec2 normalized() {
        double len = length();
        if (len <= MathUtil.EPS) {
            return Vec2.ZERO;
        }
        return new Vec2(this.x / len, this.y / len);
    }

    public double distanceSquaredTo(Vec2 b) {
        if (b == null) throw new NullPointerException("b must not be null");
        double dx = this.x - b.x;
        double dy = this.y - b.y;
        return dx * dx + dy * dy;
    }


    public double distanceTo(Vec2 b) {
        return Math.sqrt(distanceSquaredTo(b));
    }

    public Vec2 linearInterpolation(Vec2 b, double t) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        if (t < 0.0) t = 0.0;
        else if (t > 1.0) t = 1.0;

        double nx = this.x + (b.x - this.x) * t;
        double ny = this.y + (b.y - this.y) * t;
        return new Vec2(nx, ny);
    }


    public boolean epsEquals(Vec2 b, double eps) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        if (eps < 0) throw new IllegalArgumentException("eps must be >= 0");
        return Math.abs(this.x - b.x) <= eps && Math.abs(this.y - b.y) <= eps;
    }
    @Override
    public String toString() {
        return "Vec2(" + x + ", " + y + ")";
    }

}
























