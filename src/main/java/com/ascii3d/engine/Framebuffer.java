package com.ascii3d.engine;

public class Framebuffer {

    public final int width;
    public final int height;

    private final double[] intensity;
    private final double[] depth;

    public Framebuffer(int width, int height) {
        this.width     = width;
        this.height    = height;
        this.intensity = new double[width * height];
        this.depth     = new double[width * height];
    }

    public void clear() {
        for (int i = 0; i < intensity.length; i++) {
            intensity[i] = 0.0;
            depth[i]     = Double.MAX_VALUE;
        }
    }

    public boolean writePixel(int x, int y, double z, double value) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        int idx = y * width + x;
        if (z >= depth[idx]) return false;
        depth[idx]     = z;
        intensity[idx] = Math.max(0, Math.min(1, value));
        return true;
    }

    public double getIntensity(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return intensity[y * width + x];
    }

    public double getDepth(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return Double.MAX_VALUE;
        return depth[y * width + x];
    }
}
