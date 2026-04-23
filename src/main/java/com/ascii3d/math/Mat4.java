package com.ascii3d.math;


public final class Mat4 {


    private final double[][] m;

    private Mat4(double[][] m) { this.m = m; }



    public static Mat4 identity() {
        return new Mat4(new double[][]{
            {1,0,0,0},
            {0,1,0,0},
            {0,0,1,0},
            {0,0,0,1}
        });
    }

    public static Mat4 translation(double tx, double ty, double tz) {
        double[][] m = identity().m.clone();
        m = copyOf(identity().m);
        m[3][0] = tx; m[3][1] = ty; m[3][2] = tz;
        return new Mat4(m);
    }

    public static Mat4 translation(Vec3 t) {
        return translation(t.x, t.y, t.z);
    }

    public static Mat4 scale(double sx, double sy, double sz) {
        double[][] m = copyOf(identity().m);
        m[0][0] = sx; m[1][1] = sy; m[2][2] = sz;
        return new Mat4(m);
    }

    public static Mat4 scale(double s) { return scale(s, s, s); }

    public static Mat4 rotationX(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        double[][] m = copyOf(identity().m);
        m[1][1]=c; m[2][1]=-s;
        m[1][2]=s; m[2][2]= c;
        return new Mat4(m);
    }

    public static Mat4 rotationY(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        double[][] m = copyOf(identity().m);
        m[0][0]= c; m[2][0]=s;
        m[0][2]=-s; m[2][2]=c;
        return new Mat4(m);
    }

    public static Mat4 rotationZ(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        double[][] m = copyOf(identity().m);
        m[0][0]=c; m[1][0]=-s;
        m[0][1]=s; m[1][1]= c;
        return new Mat4(m);
    }

    /**
     * Standard look-at view matrix.
     * @param eye    camera position
     * @param target point to look at
     * @param up     world-up vector
     */
    public static Mat4 lookAt(Vec3 eye, Vec3 target, Vec3 up) {
        Vec3 f = target.sub(eye).normalize();
        Vec3 r = f.cross(up).normalize();
        Vec3 u = r.cross(f);

        double[][] m = copyOf(identity().m);
        m[0][0]= r.x; m[1][0]= r.y; m[2][0]= r.z;
        m[0][1]= u.x; m[1][1]= u.y; m[2][1]= u.z;
        m[0][2]=-f.x; m[1][2]=-f.y; m[2][2]=-f.z;
        m[3][0]=-r.dot(eye);
        m[3][1]=-u.dot(eye);
        m[3][2]= f.dot(eye);
        return new Mat4(m);
    }

    /**
     * Perspective projection matrix.
     * @param fovY   vertical field of view in radians
     * @param aspect width / height
     * @param near   near clip distance (positive)
     * @param far    far  clip distance (positive)
     */
    public static Mat4 perspective(double fovY, double aspect, double near, double far) {
        double f = 1.0 / Math.tan(fovY * 0.5);
        double nf = 1.0 / (near - far);
        double[][] m = new double[4][4];
        m[0][0] = f / aspect;
        m[1][1] = f;
        m[2][2] = (far + near) * nf;
        m[3][2] = 2.0 * far * near * nf;
        m[2][3] = -1.0;
        return new Mat4(m);
    }


    public Mat4 mul(Mat4 o) {
        double[][] r = new double[4][4];
        for (int col = 0; col < 4; col++)
            for (int row = 0; row < 4; row++)
                for (int k = 0; k < 4; k++)
                    r[col][row] += m[k][row] * o.m[col][k];
        return new Mat4(r);
    }

    public Vec4 mul(Vec4 v) {
        return new Vec4(
            m[0][0]*v.x + m[1][0]*v.y + m[2][0]*v.z + m[3][0]*v.w,
            m[0][1]*v.x + m[1][1]*v.y + m[2][1]*v.z + m[3][1]*v.w,
            m[0][2]*v.x + m[1][2]*v.y + m[2][2]*v.z + m[3][2]*v.w,
            m[0][3]*v.x + m[1][3]*v.y + m[2][3]*v.z + m[3][3]*v.w
        );
    }

    public Vec3 transformPoint(Vec3 v) {
        return mul(Vec4.point(v)).perspectiveDivide();
    }

    public Vec3 transformDir(Vec3 v) {
        return mul(Vec4.direction(v)).xyz();
    }


    private static double[][] copyOf(double[][] src) {
        double[][] dst = new double[4][4];
        for (int i = 0; i < 4; i++) dst[i] = src[i].clone();
        return dst;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (int row = 0; row < 4; row++) {
            sb.append("[ ");
            for (int col = 0; col < 4; col++)
                sb.append(String.format("%8.3f ", m[col][row]));
            sb.append("]\n");
        }
        return sb.toString();
    }
}
