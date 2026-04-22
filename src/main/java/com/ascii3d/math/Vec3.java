package com.ascii3d.math;

/**
 * Immutable 3D vector with full math operations.
 */
public final class Vec3 {
    public final double x, y, z;

    public static final Vec3 ZERO    = new Vec3(0, 0, 0);
    public static final Vec3 ONE     = new Vec3(1, 1, 1);
    public static final Vec3 UP      = new Vec3(0, 1, 0);
    public static final Vec3 FORWARD = new Vec3(0, 0, 1);
    public static final Vec3 RIGHT   = new Vec3(1, 0, 0);

    public Vec3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vec3 add(Vec3 o)       { return new Vec3(x+o.x, y+o.y, z+o.z); }
    public Vec3 sub(Vec3 o)       { return new Vec3(x-o.x, y-o.y, z-o.z); }
    public Vec3 mul(double s)     { return new Vec3(x*s,   y*s,   z*s);   }
    public Vec3 div(double s)     { return mul(1.0 / s); }
    public Vec3 negate()          { return mul(-1); }

    public double dot(Vec3 o)     { return x*o.x + y*o.y + z*o.z; }
    public double lengthSq()      { return dot(this); }
    public double length()        { return Math.sqrt(lengthSq()); }
    public Vec3 normalize()       { double l = length(); return l < 1e-10 ? ZERO : div(l); }

    public Vec3 cross(Vec3 o) {
        return new Vec3(
            y*o.z - z*o.y,
            z*o.x - x*o.z,
            x*o.y - y*o.x
        );
    }

    /** Linear interpolation */
    public Vec3 lerp(Vec3 o, double t) {
        return add(o.sub(this).mul(t));
    }

    /** Reflect this vector around a normal */
    public Vec3 reflect(Vec3 normal) {
        return this.sub(normal.mul(2.0 * this.dot(normal)));
    }

    @Override
    public String toString() {
        return String.format("Vec3(%.3f, %.3f, %.3f)", x, y, z);
    }
}
