package com.mperpetuo.openglgallery.old_input;

// intermediate state of touch
public class InputState {
    public float currentX,currentY,lastX,lastY;
    public int id;
    public InputState() {
        id = -1;
    }
}
