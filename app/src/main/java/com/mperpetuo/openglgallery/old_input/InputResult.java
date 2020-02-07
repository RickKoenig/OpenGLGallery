package com.mperpetuo.openglgallery.old_input;

// final result of touch, all calculated
public class InputResult {
    public float x;
    public float y;
    public float r;
    public float s; // position rotation scale

    public InputResult(float xa, float ya, float ra, float sa) {
        x = xa;
        y = ya;
        r = ra;
        s = sa;
    }

    public InputResult() {
        s = 1.0f;
    }

    InputResult(InputResult in) {
        x = in.x;
        y = in.y;
        r = in.r;
        s = in.s;
    }
}

