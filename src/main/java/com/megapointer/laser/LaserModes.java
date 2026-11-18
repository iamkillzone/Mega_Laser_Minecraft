package com.megapointer.laser;

/** Nine themes by ten shapes = 90 distinct selectable attacks. */
public final class LaserModes {
    public static final String[] THEMES = {
        "Plasma", "Inferno", "Cryo", "Void", "Solar",
        "Toxic", "Storm", "Rainbow", "Quantum"
    };
    public static final String[] SHAPES = {
        "Orb", "Cube", "Ring", "Shockwave", "Column",
        "Drill", "Cross", "Cone", "Helix", "Fracture"
    };
    public static final int[] THEME_RGB = {
        0x3AF2FF, 0xFF552B, 0x75C9FF, 0xA45AFF, 0xFFE25F,
        0x8BFF5A, 0xBEE9FF, 0xFF57D6, 0xF57CFF
    };
    private static final double[] DENSITY = {
        0.52,0.42,0.37,0.14,0.20,0.09,0.15,0.13,0.20,0.29
    };
    private LaserModes() {}
    public static int clamp(int mode) { return Math.max(0, Math.min(89, mode)); }
    public static int theme(int mode) { return clamp(mode)/10; }
    public static int shape(int mode) { return clamp(mode)%10; }
    public static String name(int mode) {
        return THEMES[theme(mode)]+" "+SHAPES[shape(mode)];
    }
    public static int limit(int mode) {
        return 100+(int)Math.round(clamp(mode)*99900.0/89.0);
    }
    public static int radius(int mode) {
        return Math.min(120, Math.max(4, (int)Math.ceil(
            Math.cbrt(limit(mode)*1.35/(8.0*DENSITY[shape(mode)])))));
    }
    public static boolean inside(int shape,double x,double y,double z) {
        double h=Math.hypot(x,z), d2=x*x+y*y+z*z;
        double ax=Math.abs(x), ay=Math.abs(y), az=Math.abs(z);
        return switch(shape) {
            case 0 -> d2<=1;
            case 1 -> Math.max(ax,Math.max(ay,az))<=0.75;
            case 2 -> d2<=1 && d2>=0.3025;
            case 3 -> ay<=0.17 && h<=1;
            case 4 -> h<=0.50 && ay<=1;
            case 5 -> h<=0.33 && ay<=1;
            case 6 -> (ax<0.27&&ay<0.27)||(ax<0.27&&az<0.27)||(ay<0.27&&az<0.27);
            case 7 -> y>=-1&&y<=1&&h<=(1-y)*0.34;
            case 8 -> {
                if(ay>1||Math.abs(h-0.65)>0.22) yield false;
                double a=Math.atan2(z,x)-y*Math.PI*4;
                double diff=Math.atan2(Math.sin(a),Math.cos(a));
                yield Math.abs(diff)<0.85||Math.abs(Math.abs(diff)-Math.PI)<0.85;
            }
            case 9 -> d2<=1&&Math.sin(19*x+8*y)*Math.cos(17*z-13*y)>-0.12;
            default -> false;
        };
    }
}
