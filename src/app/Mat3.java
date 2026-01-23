package math;

import java.util.Arrays;

/**
 * Mat3 — матрица 3x3 (обычно для нормалей/ориентации, без переноса).
 * Хранение: m[row][col]
 */
public final class Mat3 {
    private final double[][] m;

    private Mat3(double[][] m) {
        this.m = m;
    }

    /** Единичная 3x3 */
    public static Mat3 identity() {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) r[i][i] = 1.0;
        return new Mat3(r);
    }

    /** Создание из массива 3x3 (делает deep copy) */
    public static Mat3 fromArray(double[][] a) {
        if (a == null) throw new NullPointerException("a must not be null");
        if (a.length != 3) throw new IllegalArgumentException("array must be 3x3");
        for (int i = 0; i < 3; i++) {
            if (a[i] == null || a[i].length != 3) throw new IllegalArgumentException("array must be 3x3");
        }
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(a[i], 0, r[i], 0, 3);
        }
        return new Mat3(r);
    }

    /** Вырезать верхний левый 3x3 блок из Mat4 (для линейной части: rotate/scale) */
    public static Mat3 fromMat4UpperLeft(Mat4 a) {
        if (a == null) throw new NullPointerException("a must not be null");
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = a.get(i, j);
            }
        }
        return new Mat3(r);
    }

    public double get(int row, int col) {
        return m[row][col];
    }

    /** Mat3 * Vec3 (column-vector) */
    public Vec3 multiply(Vec3 v) {
        if (v == null) throw new NullPointerException("v must not be null");
        double rx = m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z;
        double ry = m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z;
        double rz = m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z;
        return new Vec3(rx, ry, rz);
    }

    /** Mat3 * Mat3 */
    public Mat3 multiply(Mat3 b) {
        if (b == null) throw new NullPointerException("b must not be null");
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double c = 0.0;
                for (int k = 0; k < 3; k++) {
                    c += this.m[i][k] * b.m[k][j];
                }
                r[i][j] = c;
            }
        }
        return new Mat3(r);
    }

    /** Транспонирование */
    public Mat3 transpose() {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = m[j][i];
            }
        }
        return new Mat3(r);
    }

    /** Детерминант 3x3 */
    public double determinant() {
        double a = m[0][0], b = m[0][1], c = m[0][2];
        double d = m[1][0], e = m[1][1], f = m[1][2];
        double g = m[2][0], h = m[2][1], i = m[2][2];
        return a * (e * i - f * h)
                - b * (d * i - f * g)
                + c * (d * h - e * g);
    }

    /** Обратная матрица (throws если вырожденная) */
    public Mat3 inverse() {
        double det = determinant();
        if (Math.abs(det) <= MathUtil.EPS) {
            throw new ArithmeticException("matrix is singular (det ~= 0)");
        }
        double invDet = 1.0 / det;

        double a = m[0][0], b = m[0][1], c = m[0][2];
        double d = m[1][0], e = m[1][1], f = m[1][2];
        double g = m[2][0], h = m[2][1], i = m[2][2];

        // adjugate (кофакторы, транспонированные)
        double[][] r = new double[3][3];
        r[0][0] =  (e * i - f * h) * invDet;
        r[0][1] = -(b * i - c * h) * invDet;
        r[0][2] =  (b * f - c * e) * invDet;

        r[1][0] = -(d * i - f * g) * invDet;
        r[1][1] =  (a * i - c * g) * invDet;
        r[1][2] = -(a * f - c * d) * invDet;

        r[2][0] =  (d * h - e * g) * invDet;
        r[2][1] = -(a * h - b * g) * invDet;
        r[2][2] =  (a * e - b * d) * invDet;

        return new Mat3(r);
    }

    /** Часто нужно для нормалей: (M^-1)^T */
    public Mat3 inverseTranspose() {
        return inverse().transpose();
    }

    public boolean epsEquals(Mat3 b, double eps) {
        if (b == null) throw new NullPointerException("b must not be null");
        if (eps < 0.0) throw new IllegalArgumentException("eps must be >= 0");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Math.abs(this.m[i][j] - b.m[i][j]) > eps) return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(m);
    }
}
