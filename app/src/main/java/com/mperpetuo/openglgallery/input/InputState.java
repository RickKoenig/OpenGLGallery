package com.mperpetuo.openglgallery.input;

/**
 * Created by cyberrickers on 3/18/2016.
 */
public class InputState {
    public float x,y; // 0,0 to resx,resy upper left to lower right
    public float fx,fy; // -asp,-1 to +asp,+1 lower left to upper right for landscape (asp >= 1), -1,-1/asp to +1,+1/asp for portrait (asp < 1)
    public float dx,dy;
    public float dfx,dfy;
    public int touch; // number of touches since UP, 0 is up (no touch) > 0 is how many frames touched

    public InputState(InputState inputState) {
        // clone
        x = inputState.x;
        y = inputState.y;
        fx = inputState.fx;
        fy = inputState.fy;
        dx = inputState.dx;
        dy = inputState.dy;
        dfx = inputState.dfx;
        dfy = inputState.dfy;
        touch = inputState.touch;
    }

    public InputState() {
    }
}
