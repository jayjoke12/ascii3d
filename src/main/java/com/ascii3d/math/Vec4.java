package com.ascii3d.math;

/**
 * 4D homogeneous coordinate vector (x, y, z, w).
 * Used for matrix multiplication and perspective divide.
 */
public final class Vec4 {
    public final double x, y, z, w;

    public Vec4(double x, double y, double z, double w) {
        this.x = x; this.y = y; this.z = z; this.w = w;
    }

    public static Vec4 point(Vec3 v)     { return new Vec4(v.x, v.y, v.z, 1.0); }
    public static Vec4 direction(Vec3 v) { return new Vec4(v.x, v.y, v.z, 0.0); }

    /** Perspective divide: convert clip-space → NDC */
    public Vec3 perspectiveDivide() {
        if (Math.abs(w) < 1e-10) return Vec3.ZERO;
        return new Vec3(x / w, y / w, z / w);
    }

    public Vec3 xyz() { return new Vec3(x, y, z); }

    public Vec4 add(Vec4 o) { return new Vec4(x+o.x, y+o.y, z+o.z, w+o.w); }
    public Vec4 mul(double s) { return new Vec4(x*s, y*s, z*s, w*s); }

    @Override
    public String toString() {
        return String.format("Vec4(%.3f, %.3f, %.3f, %.3f)", x, y, z, w);
    }
}
