package com.ascii3d.scene;

import com.ascii3d.math.Mat4;
import com.ascii3d.math.Vec3;

/**
 * TRS (Translate-Rotate-Scale) transform for a scene object.
 */
public class Transform {

    public Vec3 position;
    public Vec3 rotation;  // Euler angles in radians (X, Y, Z order)
    public Vec3 scale;

    public Transform() {
        this(Vec3.ZERO, Vec3.ZERO, Vec3.ONE);
    }

    public Transform(Vec3 position, Vec3 rotation, Vec3 scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale    = scale;
    }

    /**
     * Build the model matrix: scale → rotX → rotY → rotZ → translate
     */
    public Mat4 toMatrix() {
        Mat4 S  = Mat4.scale(scale.x, scale.y, scale.z);
        Mat4 Rx = Mat4.rotationX(rotation.x);
        Mat4 Ry = Mat4.rotationY(rotation.y);
        Mat4 Rz = Mat4.rotationZ(rotation.z);
        Mat4 T  = Mat4.translation(position);
        // Apply in order: S → Rx → Ry → Rz → T
        return T.mul(Rz).mul(Ry).mul(Rx).mul(S);
    }
}
