package com.ascii3d.renderer;


public enum AsciiPalette {


    DETAILED("$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. "),


    CLASSIC(" .:-=+*#%@"),


    COMPACT(" .:!|fI10XM"),


    BLOCK("  ░░▒▒▓▓██");

    private final char[] chars;

    AsciiPalette(String palette) {

        String rev = new StringBuilder(palette).reverse().toString();
        this.chars = rev.toCharArray();
    }


    public char map(double intensity) {
        int idx = (int) Math.round(intensity * (chars.length - 1));
        idx = Math.max(0, Math.min(chars.length - 1, idx));
        return chars[idx];
    }


    public int levels() { return chars.length; }
}
