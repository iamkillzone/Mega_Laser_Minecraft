package com.megapointer.laser;
public final class LaserCinematics {
    public static final int FIRST_MODE=85, INTRO_TICKS=100;
    private static final String[] TITLES={
        "ABYSSAL DRILL","AXIS OF ANNIHILATION","EVENT HORIZON",
        "TWIN SINGULARITIES","REALITY FRACTURE"};
    private static final String[] SUBS={
        "The core bores through the world","Six beams align on the epicenter",
        "The horizon collapses","Two singularities intertwine",
        "Reality begins to fracture"};
    private LaserCinematics(){}
    public static boolean enabled(int mode){return mode>=FIRST_MODE&&mode<90;}
    public static String title(int mode){return enabled(mode)?TITLES[mode-85]:"LASER STRIKE";}
    public static String subtitle(int mode){return enabled(mode)?SUBS[mode-85]:"";}
}
