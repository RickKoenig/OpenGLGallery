package com.mperpetuo.openglgallery.enga;

public class Colors {
    private static float[] convert(int r,int g, int b) {
        float inv = 1.0f/255.0f;
        float[] ret = {r*inv,g*inv,b*inv,1.0f};
        return ret;
    }
    public static final float[] F32BLACK = convert(0,0,0);
    public static final float[] F32BLUE = convert(0,0,170);
    public static final float[] F32GREEN = convert(0,170,0);
    public static final float[] F32CYAN = convert(0,170,170);

    public static final float[] F32RED = convert(170,0,0);
    public static final float[] F32MAGENTA = convert(170,0,170);
    public static final float[] F32BROWN = convert(170,85,0);
    public static final float[] F32LIGHTGRAY = convert(170,170,170);

    public static final float[] F32DARKGRAY = convert(85,85,85);
    public static final float[] F32LIGHTBLUE = convert(85,85,255);
    public static final float[] F32LIGHTGREEN = convert(85,255,85);
    public static final float[] F32LIGHTCYAN = convert(85,255,255);

    public static final float[] F32LIGHTRED = convert(255,85,85);
    public static final float[] F32LIGHTMAGENTA = convert(255,85,255);
    public static final float[] F32YELLOW = convert(255,255,85);
    public static final float[] F32WHITE = convert(255,255,255);
    /*
// RGBA
    A32BLACK  = [0,0,0];
    A32BLUE = [0,0,170];
    A32GREEN = [0,170,0];
    A32CYAN = [0,170,170];

    A32RED = [170,0,0];
    A32MAGENTA = [170,0,170];
    A32BROWN = [170,85,0];
    A32LIGHTGRAY = [170,170,170];

    A32DARKGRAY	= [85,85,85];
    A32LIGHTBLUE = [85,85,255];
    A32LIGHTGREEN = [85,255,85];
    A32LIGHTCYAN = [85,255,255];

    A32LIGHTRED = [255,85,85];
    A32LIGHTMAGENTA	= [255,85,255];
    A32YELLOW = [255,255,85];
    A32WHITE = [255,255,255];


    F32BLACK  = F32(A32BLACK);
    F32BLUE = F32(A32BLUE);
    F32GREEN = F32(A32GREEN);
    F32CYAN = F32(A32CYAN);

    F32RED = F32(A32RED);
    F32MAGENTA = F32(A32MAGENTA);
    F32BROWN = F32(A32BROWN);
    F32LIGHTGRAY = F32(A32LIGHTGRAY);

    F32DARKGRAY	= F32(A32DARKGRAY);
    F32LIGHTBLUE = F32(A32LIGHTBLUE);
    F32LIGHTGREEN = F32(A32LIGHTGREEN);
    F32LIGHTCYAN = F32(A32LIGHTCYAN);

    F32LIGHTRED = F32(A32LIGHTRED);
    F32LIGHTMAGENTA	= F32(A32LIGHTMAGENTA);
    F32YELLOW = F32(A32YELLOW);
    F32WHITE = F32(A32WHITE); */
}
