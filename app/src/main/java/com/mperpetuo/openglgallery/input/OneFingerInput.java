package com.mperpetuo.openglgallery.input;

import android.util.Log;
import android.view.MotionEvent;

import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.old_input.*;

/**
 * Created by cyberrickers on 3/17/2016.
 */

public class OneFingerInput {
    static final String TAG = "OneFingerInput";

    InputState inputState = new InputState();
    float lastX,lastY;

    float[] iToF(float x,float y) {
        float[] ret = new float[2];
        ret[0] = 2.0f*x/Main3D.viewWidth - 1.0f;
        ret[1] = -2.0f*y/Main3D.viewHeight + 1.0f; // flip y
        if (Main3D.viewAsp > 1.0f) {
            ret[0] *= Main3D.viewAsp;
        } else {
            ret[1] /= Main3D.viewAsp;
        }
        return ret;
    }

    float[] iToFv(float x,float y) {
        float[] ret = new float[2];
        ret[0] = 2.0f*x/Main3D.viewWidth;
        ret[1] = -2.0f*y/Main3D.viewHeight; // flip y
        if (Main3D.viewAsp > 1.0f) {
            ret[0] *= Main3D.viewAsp;
        } else {
            ret[1] /= Main3D.viewAsp;
        }
        return ret;
    }

    synchronized public void readInput(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        float[] F = iToF(x,y);
        float fx = F[0];
        float fy = F[1];
        int action = e.getActionMasked();
        String actionName = getActionName(action);
        //Log.d(TAG, "One Finger MotionEvent " + actionName + " X = " + x + " Y = " + y);

        switch(action) {
            // DOWN
            // MOVE
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: // process move events elsewhere, not as fined grained as up or down
                if (inputState.touch == 0) { // going down, reset last to current
                    lastX = x;
                    lastY = y;
                }
                float dx = x - lastX;
                float dy = y - lastY;
                //Log.d(TAG,"x = " + x + " lastX = " + lastX + " dx = " + dx + " dy = " + dy);
                float[] Fd = iToFv(dx,dy);
                float dfx = Fd[0];
                float dfy = Fd[1];
                ++inputState.touch;
                inputState.x = x;
                inputState.y = y;
                inputState.fx = fx;
                inputState.fy = fy;
                inputState.dx += dx;
                inputState.dy += dy;
                inputState.dfx += dfx;
                inputState.dfy += dfy;
                break;
            // UP
            case MotionEvent.ACTION_UP:
                inputState.touch = 0;
                inputState.x = -1.0f;
                inputState.y = -1.0f;
                break;
        }
        lastX = x;
        lastY = y;
    }

    synchronized public InputState getResult() {
        InputState ret = new InputState(inputState); // clone
        inputState.dx = 0;
        inputState.dy = 0;
        inputState.dfx = 0;
        inputState.dfy = 0;
        return ret;
    }

    private String getActionName(int actionType) {
        switch(actionType) {
            case MotionEvent.ACTION_DOWN:
                return "DOWN";
            case MotionEvent.ACTION_POINTER_DOWN:
                return "POINTER_DOWN";
            case MotionEvent.ACTION_MOVE:
                return "MOVE";
            case MotionEvent.ACTION_POINTER_UP:
                return "POINTER_UP";
            case MotionEvent.ACTION_UP:
                return "UP";
        }
        return "UNKNOWN EVENT " + actionType + " ";
    }

}
