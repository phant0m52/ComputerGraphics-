package math;

import java.util.Arrays;

public final class Mat4 {

    private final double[][] m;

    private Mat4(double[][] m){
        this.m = m;
    }

    public static Mat4 identity(){
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) r[i][i] = 1.0;
        return new Mat4(r);
    }

    /** Перспективная матрица (column-vector): v' = M * v */
    public static Mat4 perspective(double fovYRad, double aspect, double zNear, double zFar) {
        double f = 1.0 / Math.tan(fovYRad / 2.0);
        double[][] r = new double[4][4];

        r[0][0] = f / aspect;
        r[1][1] = f;
        r[2][2] = (zFar + zNear) / (zNear - zFar);
        r[2][3] = (2.0 * zFar * zNear) / (zNear - zFar);
        r[3][2] = -1.0;
        r[3][3] = 0.0;

        return new Mat4(r);
    }

    public Vec4 multiply(Vec4 v){
        if (v == null) throw new NullPointerException("v must not be null");

        double rx = m[0][0]*v.x + m[0][1]*v.y + m[0][2]*v.z + m[0][3]*v.w;
        double ry = m[1][0]*v.x + m[1][1]*v.y + m[1][2]*v.z + m[1][3]*v.w;
        double rz = m[2][0]*v.x + m[2][1]*v.y + m[2][2]*v.z + m[2][3]*v.w;
        double rw = m[3][0]*v.x + m[3][1]*v.y + m[3][2]*v.z + m[3][3]*v.w;
        return new Vec4(rx, ry, rz, rw);
    }

    public Mat4 multiply(Mat4 b){
        if (b == null) throw new NullPointerException("b must not be null");
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++) {
                double c = 0.0;
                for (int k = 0; k < 4; k++) {
                    c += this.m[i][k] * b.m[k][j];
                }
                r[i][j] = c;
            }
        }
        return new Mat4(r);
    }

    public static Mat4 translate(double tx, double ty, double tz){
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) r[i][i] = 1.0;
        r[0][3] = tx;
        r[1][3] = ty;
        r[2][3] = tz;
        return new Mat4(r);
    }

    public static Mat4 scale(double sx, double sy, double sz) {
        double[][] r = new double[4][4];
        r[0][0] = sx;
        r[1][1] = sy;
        r[2][2] = sz;
        r[3][3] = 1;
        return new Mat4(r);
    }

    public static Mat4 rotateX(double a){
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) r[i][i] = 1.0;
        double c = Math.cos(a);
        double s = Math.sin(a);
        r[1][1] = c;
        r[1][2] = -s;
        r[2][1] = s;
        r[2][2] = c;
        return new Mat4(r);
    }

    public static Mat4 rotateY(double a){
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) r[i][i] = 1.0;
        double c = Math.cos(a);
        double s = Math.sin(a);
        r[0][0] = c;
        r[0][2] = s;
        r[2][0] = -s;
        r[2][2] = c;
        return new Mat4(r);
    }

    public static Mat4 rotateZ(double a) {
        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) r[i][i] = 1.0;

        double c = Math.cos(a);
        double s = Math.sin(a);

        r[0][0] = c;
        r[0][1] = -s;
        r[1][0] = s;
        r[1][1] = c;

        return new Mat4(r);
    }

    public boolean epsEquals(Mat4 b, double eps) {
        if (b == null) throw new NullPointerException("b must not be null");
        if (eps < 0.0) throw new IllegalArgumentException("eps must be >= 0");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(this.m[i][j] - b.m[i][j]) > eps) return false;
            }
        }
        return true;
    }

    public double get(int row, int col) { return m[row][col]; }

    @Override public String toString() { return Arrays.deepToString(m); }

    public static Mat4 fromArray(double[][] a) {
        if (a == null) throw new NullPointerException("a must not be null");
        if (a.length != 4) throw new IllegalArgumentException("array must be 4x4");
        for (int i = 0; i < 4; i++) {
            if (a[i] == null || a[i].length != 4) throw new IllegalArgumentException("array must be 4x4");
        }

        double[][] r = new double[4][4];
        for (int i = 0; i < 4; i++) System.arraycopy(a[i], 0, r[i], 0, 4);
        return new Mat4(r);
    }
}
