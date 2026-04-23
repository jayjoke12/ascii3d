package com.ascii3d.renderer;

import com.ascii3d.engine.Framebuffer;

/**
 * Converts a {@link Framebuffer} into an ASCII string for terminal output.
 */
public class AsciiRenderer {

    private final AsciiPalette palette;


    private double contrast   = 1.0;


    private double brightness = 0.0;


    private double gamma      = 1.0;



    public AsciiRenderer(AsciiPalette palette) {
        this.palette = palette;
    }

    public AsciiRenderer() {
        this(AsciiPalette.DETAILED);
    }




    public AsciiRenderer contrast(double contrast) {
        this.contrast = contrast;
        return this;
    }


    public AsciiRenderer brightness(double brightness) {
        this.brightness = brightness;
        return this;
    }


    public AsciiRenderer gamma(double gamma) {
        this.gamma = Math.max(0.01, gamma);
        return this;
    }



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


    public double processIntensity(double raw) {

        double v = (raw - 0.5) * contrast + 0.5 + brightness;

        v = Math.max(0.0, Math.min(1.0, v));

        v = Math.pow(v, gamma);
        return Math.max(0.0, Math.min(1.0, v));
    }

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


    public void renderAndPrint(Framebuffer fb, int frameNumber) {
        String frame = render(fb);
        int rows     = fb.height / 2;

        if (frameNumber == 0) {
            System.out.print("\033[?25l");
        } else {
            System.out.print("\033[" + (rows + 3) + "A");
        }

        System.out.printf(" ASCII 3D Engine  frame:%-5d  contrast:%.1f  brightness:%.2f  gamma:%.2f%n",
                frameNumber, contrast, brightness, gamma);
        System.out.print(frame);
        System.out.println(" [Ctrl+C to exit]");
        System.out.flush();
    }


    public void cleanup() {
        System.out.print("\033[?25h");
        System.out.flush();
    }


    public double getContrast()   { return contrast; }
    public double getBrightness() { return brightness; }
    public double getGamma()      { return gamma; }
    public AsciiPalette getPalette() { return palette; }
}
