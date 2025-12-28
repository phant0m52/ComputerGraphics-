package math;

import java.util.Arrays;

public final class Mat4 {
    // m[row][col]
    private final double[][] m;

    private Mat4(double[][] m) { this.m = m; }

    public static Mat4 identity() {
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) r[i][i] = 1.0;
        return new Mat4(r);
    }

    public static Mat4 of(double[][] a) {
        if (a.length != 4 || a[0].length != 4) throw new IllegalArgumentException("Mat4 must be 4x4");
        double[][] c = new double[4][4];
        for (int i = 0; i < 4; i++) System.arraycopy(a[i], 0, c[i], 0, 4);
        return new Mat4(c);
    }

    public double get(int row, int col) { return m[row][col]; }

    // C = this * b
    public Mat4 mul(Mat4 b) {
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) {          // row
            for (int j = 0; j < 4; j++) {      // col
                double s = 0.0;
                for (int k = 0; k < 4; k++) s += this.m[i][k] * b.m[k][j];
                r[i][j] = s;
            }
        }
        return new Mat4(r);
    }

    // v' = M * v  (столбцы)
    public Vec4 mul(Vec4 v) {
        double[] a = {v.x, v.y, v.z, v.w};
        double[] r = new double[4];
        for (int i = 0; i < 4; i++) {
            double s = 0.0;
            for (int k = 0; k < 4; k++) s += m[i][k] * a[k];
            r[i] = s;
        }
        return new Vec4(r[0], r[1], r[2], r[3]);
    }

    public Vec3 transformPoint(Vec3 p) {
        Vec4 r = mul(Vec4.point(p));
        if (r.w == 0.0) return r.xyz();
        return new Vec3(r.x / r.w, r.y / r.w, r.z / r.w);
    }

    public Vec3 transformDirection(Vec3 d) {
        return mul(Vec4.dir(d)).xyz();
    }

    // --- Аффинные фабрики под столбцы: p' = M * p ---

    public static Mat4 translation(double tx, double ty, double tz) {
        Mat4 r = identity();
        r.m[0][3] = tx;
        r.m[1][3] = ty;
        r.m[2][3] = tz;
        return r;
    }

    public static Mat4 scale(double sx, double sy, double sz) {
        double[][] r = new double[4][4];
        r[0][0] = sx; r[1][1] = sy; r[2][2] = sz; r[3][3] = 1.0;
        return new Mat4(r);
    }

    public static Mat4 rotationX(double angleRad) {
        double c = Math.cos(angleRad), s = Math.sin(angleRad);
        double[][] r = new double[4][4];
        r[0][0] = 1; r[3][3] = 1;
        r[1][1] = c;  r[1][2] = -s;
        r[2][1] = s;  r[2][2] = c;
        return new Mat4(r);
    }

    public static Mat4 rotationY(double angleRad) {
        double c = Math.cos(angleRad), s = Math.sin(angleRad);
        double[][] r = new double[4][4];
        r[1][1] = 1; r[3][3] = 1;
        r[0][0] = c;  r[0][2] = s;
        r[2][0] = -s; r[2][2] = c;
        return new Mat4(r);
    }

    public static Mat4 rotationZ(double angleRad) {
        double c = Math.cos(angleRad), s = Math.sin(angleRad);
        double[][] r = new double[4][4];
        r[2][2] = 1; r[3][3] = 1;
        r[0][0] = c;  r[0][1] = -s;
        r[1][0] = s;  r[1][1] = c;
        return new Mat4(r);
    }

    @Override public String toString() { return Arrays.deepToString(m); }
}
