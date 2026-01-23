package math;


public final class Vec3 {
    public final double x, y, z;
    public static final Vec3 ZERO = new Vec3(0.0,0.0,0.0);


    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vec3 zero() {
        return ZERO;
    }

    public Vec3 add(Vec3 b) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        return new Vec3(this.x + b.x, this.y + b.y, this.z + b.z);
    }

    public Vec3 sub(Vec3 b){
        if (b == null) {
            throw new NullPointerException("b must nоt be null");
        }
        return new Vec3(this.x - b.x, this.y - b.y, this.z - b.z);
    }
    public Vec3 scale(double k) {
        return new Vec3(this.x * k, this.y * k, this.z * k);
    }

    public double dot(Vec3 b) {
        if (b == null) {
            throw new NullPointerException(("b must not be null"));
        }
        return this.x * b.x + this.y * b.y + this.z * b.z;
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }


    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vec3 normalized() {
        double len = length();
        if (len <= MathUtil.EPS) {
            return Vec3.ZERO;
        }
        return new Vec3(this.x / len, this.y / len, this.z / len);
    }

    public double distanceSquaredTo(Vec3 b) {
        if (b == null) throw new NullPointerException("b must not be null");
        double dx = this.x - b.x;
        double dy = this.y - b.y;
        double dz = this.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }


    public double distanceTo(Vec3 b) {
        return Math.sqrt(distanceSquaredTo(b));
    }

    public Vec3 linearInterpolation(Vec3 b, double t) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        if (t < 0.0) t = 0.0;
        else if (t > 1.0) t = 1.0;

        double nx = this.x + (b.x - this.x) * t;
        double ny = this.y + (b.y - this.y) * t;
        double nz = this.z + (b.z - this.z) * t;
        return new Vec3(nx, ny, nz);
    }


    public boolean epsEquals(Vec3 b, double eps) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        if (eps < 0) throw new IllegalArgumentException("eps must be >= 0");
        return Math.abs(this.x - b.x) <= eps && Math.abs(this.y - b.y) <= eps && Math.abs(this.z - b.z) <= eps;
    }

    public Vec3 cross(Vec3 b) {
        if (b == null) {
            throw new NullPointerException("b must not be null");
        }
        double cx = this.y * b.z - this.z * b.y;
        double cy = this.z * b.x - this.x * b.z;
        double cz = this.x * b.y - this.y * b.x;
        return new Vec3(cx, cy, cz);
    }

    @Override
    public String toString() {
        return "Vec3(" + x + ", " + y + ", " + z + ")";
    }

}










