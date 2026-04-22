package com.ascii3d.renderer;

/**
 * Maps a normalised intensity [0..1] to an ASCII character.
 *
 * Several palettes are provided; DETAILED is good for shading,
 * BLOCK works well for bold terminal output.
 */
public enum AsciiPalette {

    /** Dense ramp from dark to bright */
    DETAILED("$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. "),

    /** Classic Braille-style ramp */
    CLASSIC(" .:-=+*#%@"),

    /** Compact ramp — good for small terminals */
    COMPACT(" .:!|fI10XM"),

    /** Block characters — high contrast */
    BLOCK("  ░░▒▒▓▓██");

    private final char[] chars;

    AsciiPalette(String palette) {
        // Reverse so index 0 = darkest
        String rev = new StringBuilder(palette).reverse().toString();
        this.chars = rev.toCharArray();
    }

    /**
     * Map intensity [0..1] → character.
     * 0.0 = darkest (background), 1.0 = brightest.
     */
    public char map(double intensity) {
        int idx = (int) Math.round(intensity * (chars.length - 1));
        idx = Math.max(0, Math.min(chars.length - 1, idx));
        return chars[idx];
    }

    /** Number of distinct characters in this palette */
    public int levels() { return chars.length; }
}
