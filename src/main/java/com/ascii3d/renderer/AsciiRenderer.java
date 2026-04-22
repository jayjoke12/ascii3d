package com.ascii3d.renderer;

import com.ascii3d.engine.Framebuffer;

/**
 * Converts a {@link Framebuffer} into an ASCII string for terminal output.
 *
 * Supports contrast, brightness, and gamma adjustments applied per-pixel
 * before palette mapping.
 *
 * Pipeline per pixel:
 *   raw intensity → brightness shift → contrast curve → gamma → palette char
 *
 * Quick presets:
 *   AsciiRenderer.defaults()           — neutral (1.0 / 0.0 / 1.0)
 *   AsciiRenderer.highContrast()       — punchy (2.0 / -0.1 / 0.8)
 *   AsciiRenderer.soft()               — gentle (0.7 /  0.1 / 1.2)
 */
public class AsciiRenderer {

    private final AsciiPalette palette;

    /** Contrast multiplier. 1.0 = neutral. >1 = more contrast, <1 = flat. */
    private double contrast   = 1.0;

    /** Brightness offset added after contrast. 0.0 = neutral. +0.2 = brighter. */
    private double brightness = 0.0;

    /** Gamma correction exponent. 1.0 = linear. <1 = brighter mids, >1 = darker. */
    private double gamma      = 1.0;

    // ── Constructors ──────────────────────────────────────────────────────────

    public AsciiRenderer(AsciiPalette palette) {
        this.palette = palette;
    }

    public AsciiRenderer() {
        this(AsciiPalette.DETAILED);
    }

    // ── Fluent setters ────────────────────────────────────────────────────────

    /**
     * Set contrast. 1.0 = neutral. Try 1.5–2.5 for crisp edges.
     */
    public AsciiRenderer contrast(double contrast) {
        this.contrast = contrast;
        return this;
    }

    /**
     * Set brightness offset. 0.0 = neutral. Range roughly -0.5 to +0.5.
     */
    public AsciiRenderer brightness(double brightness) {
        this.brightness = brightness;
        return this;
    }

    /**
     * Set gamma. 1.0 = linear. 0.5 = lift shadows, 2.0 = crush shadows.
     */
    public AsciiRenderer gamma(double gamma) {
        this.gamma = Math.max(0.01, gamma);
        return this;
    }

    // ── Preset factories ──────────────────────────────────────────────────────

    public static AsciiRenderer defaults() {
        return new AsciiRenderer();
    }

    public static AsciiRenderer highContrast() {
        return new AsciiRenderer().contrast(2.0).brightness(-0.1).gamma(0.8);
    }

    public static AsciiRenderer soft() {
        return new AsciiRenderer().contrast(0.7).brightness(0.1).gamma(1.2);
    }

    public static AsciiRenderer dramatic() {
        return new AsciiRenderer().contrast(3.0).brightness(-0.2).gamma(0.6);
    }

    // ── Core rendering ────────────────────────────────────────────────────────

    /**
     * Apply the contrast/brightness/gamma pipeline to a raw [0..1] intensity.
     *
     * Contrast is applied around the midpoint (0.5):
     *   out = (in - 0.5) * contrast + 0.5 + brightness
     * Then gamma:
     *   out = out ^ gamma
     */
    public double processIntensity(double raw) {
        // Contrast (centred on 0.5) + brightness
        double v = (raw - 0.5) * contrast + 0.5 + brightness;
        // Clamp before gamma to avoid NaN on negative values
        v = Math.max(0.0, Math.min(1.0, v));
        // Gamma
        v = Math.pow(v, gamma);
        return Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Convert the framebuffer to an ASCII art string.
     * Each character cell spans 1 pixel wide × 2 pixels tall (aspect correction).
     */
    public String render(Framebuffer fb) {
        int cols = fb.width;
        int rows = fb.height / 2;

        StringBuilder sb = new StringBuilder((cols + 1) * rows);

        for (int row = 0; row < rows; row++) {
            int y0 = row * 2;
            int y1 = Math.min(y0 + 1, fb.height - 1);

            for (int col = 0; col < cols; col++) {
                double raw = (fb.getIntensity(col, y0) + fb.getIntensity(col, y1)) * 0.5;
                sb.append(palette.map(processIntensity(raw)));
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * Render and immediately print using ANSI cursor tricks for in-place animation.
     */
    public void renderAndPrint(Framebuffer fb, int frameNumber) {
        String frame = render(fb);
        int rows     = fb.height / 2;

        if (frameNumber == 0) {
            System.out.print("\033[?25l");  // hide cursor
        } else {
            System.out.print("\033[" + (rows + 3) + "A");
        }

        System.out.printf(" ASCII 3D Engine  frame:%-5d  contrast:%.1f  brightness:%.2f  gamma:%.2f%n",
                frameNumber, contrast, brightness, gamma);
        System.out.print(frame);
        System.out.println(" [Ctrl+C to exit]");
        System.out.flush();
    }

    /** Restore terminal cursor */
    public void cleanup() {
        System.out.print("\033[?25h");
        System.out.flush();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public double getContrast()   { return contrast; }
    public double getBrightness() { return brightness; }
    public double getGamma()      { return gamma; }
    public AsciiPalette getPalette() { return palette; }
}
