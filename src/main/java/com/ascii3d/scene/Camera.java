package com.ascii3d.scene;

import com.ascii3d.math.Mat4;
import com.ascii3d.math.Vec3;

/**
 * Perspective camera: produces view + projection matrices.
 */
public class Camera {

    public Vec3   position;
    public Vec3   target;
    public Vec3   up;
    public double fovY;
    public double aspect;
    public double near;
    public double far;

    public Camera(Vec3 position, Vec3 target, double fovY, double aspect,
                  double near, double far) {
        this.position = position;
        this.target   = target;
        this.up       = Vec3.UP;
        this.fovY     = fovY;
        this.aspect   = aspect;
        this.near     = near;
        this.far      = far;
    }

    public Mat4 viewMatrix()       { return Mat4.lookAt(position, target, up); }
    public Mat4 projectionMatrix() { return Mat4.perspective(fovY, aspect, near, far); }


    public Mat4 viewProjection()   { return projectionMatrix().mul(viewMatrix()); }

    public void orbitY(double delta) {
        Vec3 offset = position.sub(target);
        Mat4 rot = Mat4.rotationY(delta);
        position = target.add(rot.transformDir(offset));
    }


    public void orbitX(double delta) {
        Vec3 right  = position.sub(target).normalize().cross(up).normalize();
        Vec3 offset = position.sub(target);
        // Axis-angle rotation around 'right'
        double c = Math.cos(delta), s = Math.sin(delta);
        Vec3 newOffset = offset.mul(c)
                .add(right.cross(offset).mul(s))
                .add(right.mul(right.dot(offset) * (1 - c)));
        position = target.add(newOffset);
    }
}
