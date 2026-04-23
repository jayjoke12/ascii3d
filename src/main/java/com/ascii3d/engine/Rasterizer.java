package com.ascii3d.engine;

import com.ascii3d.math.Vec3;


public class Rasterizer {

    private final Framebuffer fb;

    public Rasterizer(Framebuffer fb) {
        this.fb = fb;
    }

    /**
     * Rasterize one triangle into the framebuffer.
     *
     * @param p0,p1,p2   screen-space positions (x,y in pixels; z in NDC [-1..1])
     * @param i0,i1,i2   per-vertex Phong intensity (already computed by vertex shader)
     */
    public void drawTriangle(Vec3 p0, Vec3 p1, Vec3 p2,
                             double i0, double i1, double i2) {

        double signedArea = edgeFunction(p0, p1, p2);
        if (signedArea <= 0) return;

        int minX = Math.max(0, (int) Math.floor(Math.min(p0.x, Math.min(p1.x, p2.x))));
        int maxX = Math.min(fb.width  - 1, (int) Math.ceil (Math.max(p0.x, Math.max(p1.x, p2.x))));
        int minY = Math.max(0, (int) Math.floor(Math.min(p0.y, Math.min(p1.y, p2.y))));
        int maxY = Math.min(fb.height - 1, (int) Math.ceil (Math.max(p0.y, Math.max(p1.y, p2.y))));

        for (int py = minY; py <= maxY; py++) {
            for (int px = minX; px <= maxX; px++) {
                Vec3 p = new Vec3(px + 0.5, py + 0.5, 0);

                double w0 = edgeFunction(p1, p2, p) / signedArea;
                double w1 = edgeFunction(p2, p0, p) / signedArea;
                double w2 = edgeFunction(p0, p1, p) / signedArea;

                if (w0 < 0 || w1 < 0 || w2 < 0) continue;

                double z         = w0 * p0.z + w1 * p1.z + w2 * p2.z;
                double intensity = w0 * i0    + w1 * i1    + w2 * i2;

                fb.writePixel(px, py, z, intensity);
            }
        }
    }

    private static double edgeFunction(Vec3 a, Vec3 b, Vec3 c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }
}
