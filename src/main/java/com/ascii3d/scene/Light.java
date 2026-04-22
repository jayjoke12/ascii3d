package com.ascii3d.scene;

import com.ascii3d.math.Vec3;

/**
 * Simple directional light with ambient + diffuse + specular components.
 */
public class Light {

    public Vec3   direction;   // normalized, points FROM light TO scene
    public double ambient;     // [0..1]
    public double diffuse;     // [0..1]
    public double specular;    // [0..1]
    public int    shininess;   // Phong exponent

    public Light(Vec3 direction, double ambient, double diffuse,
                 double specular, int shininess) {
        this.direction = direction.normalize();
        this.ambient   = ambient;
        this.diffuse   = diffuse;
        this.specular  = specular;
        this.shininess = shininess;
    }

    /** Default pleasant directional light */
    public static Light defaultLight() {
        return new Light(new Vec3(-1, -2, -1), 0.15, 0.75, 0.25, 16);
    }

    /**
     * Full Phong lighting model.
     * @param normal    surface normal (world space, normalized)
     * @param viewDir   direction from surface to camera (normalized)
     * @return intensity in [0..1]
     */
    public double phong(Vec3 normal, Vec3 viewDir) {
        Vec3   toLight  = direction.negate();            // direction toward light
        double diff     = Math.max(0, normal.dot(toLight));
        Vec3   halfVec  = toLight.add(viewDir).normalize();
        double spec     = Math.pow(Math.max(0, normal.dot(halfVec)), shininess);
        return Math.min(1.0, ambient + diffuse * diff + specular * spec);
    }
}
