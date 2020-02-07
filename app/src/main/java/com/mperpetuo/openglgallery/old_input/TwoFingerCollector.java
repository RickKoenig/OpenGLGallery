package com.mperpetuo.openglgallery.old_input;

import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by rickkoenig on 1/7/16.
 */
public class TwoFingerCollector {
    private final static String TAG = "TwoFingerCollector";

    // size of this view on display and memory
    int width,height;
    float aspect;

    // touch input
    private int maxFingers = 10;

    // normalized index input
    private float fingersX[] = new float[maxFingers];
    private float fingersY[] = new float[maxFingers];

    // processed current pos and last pos, no big jumps
    private int maxInputState = 2;
    InputState[] inputState = new InputState[maxInputState];
    InputState oneInput;
    boolean twoFingers;


    InputResult inputResult = new InputResult(0.0f,0.0f,0.0f,1.0f); // x y rotation(degrees) scale

    public TwoFingerCollector() {
        int i;
        for (i=0;i<maxInputState;++i) {
            inputState[i] = new InputState();
        }
    }

    synchronized public void setResult(float x,float y,float rot,float scale) {
        inputResult.x = x;
        inputResult.y = y;
        inputResult.r = rot;
        inputResult.s = scale;
    }

    synchronized public void readInput(View v, MotionEvent e) {
        width = v.getWidth();
        height = v.getHeight();
        aspect = (float)width/height;
        float x,y;
        x = e.getX();
        y = e.getY();
        String actionName = getActionName(e.getActionMasked());
        Log.e(TAG,"MotionEvent " + actionName + " X = " + x + "/" + v.getWidth() + " Y = " + y + "/" + v.getHeight());

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        int numIndex = Math.min(e.getPointerCount(), maxFingers);
        int i,j;

        //Log.e(TAG, "touch event ------- pointer count = " + numIndex);
        //       for (i=0;i<numIndex;++i) {
        //           Log.e(TAG,"    pointer " + i + ": value = " + e.getX(i) + " " + e.getY(i));
        //      }

        // convert all coordinate inputs to normalized, store in fingers array, compensate for destAspect ratio
        for (i=0;i<numIndex;++i) {
            x = e.getX(i);
            y = e.getY(i);
            if (aspect > 1.0f) { // landscape
                x = 2.0f*(x - width/2.0f)/height;
                y = -2.0f*(y - height/2.0f)/height;
            } else { // portrait
                x = 2.0f*(x - width/2.0f)/width;
                y = -2.0f*(y - height/2.0f)/width;
            }
            fingersX[i] = x;
            fingersY[i] = y;
        }

        // multi touch event, up and down are fine grained, better process all fingers for move events
        int index = e.getActionIndex(); // up or down but not move
        int id = e.getPointerId(index); // up or down but not move
        int action = e.getActionMasked(); // up or down, else move on every index
        //Log.e(TAG,"touch " + getActionName(action) + " " + " index " + index + " id " + id + " " + " X0 " + fingersX[0] + " Y0 " + fingersY[0]);

        // process action
        switch (action) {
            // DOWN
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                // find an empty slot
                for (i=0;i<maxInputState;++i) {
                    if (inputState[i].id == -1) {
                        Log.e(TAG,getActionName(action) + ": setting inputState[" + i + "].id from -1 to " + id);
                        inputState[i].id = id;
                        inputState[i].currentX = inputState[i].lastX = fingersX[index];
                        inputState[i].currentY = inputState[i].lastY = fingersY[index];
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE: // process move events elsewhere, not as fined grained as up or down
                break;
            // UP
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                int upIdx = -1;
                for (i=0;i<maxInputState;++i) {
                    // see if id is already in the inputState list
                    if (inputState[i].id == id) {
                        upIdx = i;
                        break;
                    }
                }
                if (upIdx >= 0) {
                    // mark as free
                    Log.e(TAG,getActionName(action) + ": setting inputState[" + upIdx + "].id from " + inputState[upIdx].id + " to -1");
                    inputState[upIdx].id = -1;
                    // try to find another id to track that is not being tracked
                    int altid = -1;
                    for (i = 0; i < numIndex; ++i) {
                        altid = e.getPointerId(i); // possible alternate id
                        for (j = 0; j < maxInputState; ++j) { // make sure not already being tracked and not the one we just freed
                            if (inputState[j].id == altid || id == altid) {
                                break; // already in use
                            }
                        }
                        if (j == maxInputState) { // use altid, no previous usage found
                            break;
                        }
                    }
                    if (i != numIndex) {
                        // switch to this id
                        Log.e(TAG,"  UP: setting inputState[" + upIdx + "].id from " + inputState[upIdx].id + " to " + altid);
                        inputState[upIdx].id = altid;
                        inputState[upIdx].lastX = inputState[upIdx].currentX = fingersX[i];
                        inputState[upIdx].lastY = inputState[upIdx].currentY = fingersY[i];
                    }
                    break;
                }
        }

        // MOVE
        for (i=0;i<maxInputState;++i) {
            if (inputState[i].id >= 0) {
                index = e.findPointerIndex(inputState[i].id);
                if (index >= 0) {
                    inputState[i].lastX = inputState[i].currentX;
                    inputState[i].lastY = inputState[i].currentY;
                    inputState[i].currentX = fingersX[index];
                    inputState[i].currentY = fingersY[index];
                }
            }
        }
        oneInput = null;
        twoFingers = false;
        if (inputState[0].id >=0 && inputState[1].id >=0) {
            // two fingers
            twoFingers = true;
        } else if (inputState[0].id >=0) {
            // one finger
            oneInput = inputState[0];
        } else if (inputState[1].id >= 0) {
            // one finger
            oneInput = inputState[1];
        } else {
            // zero fingers
        }
        calculate2();
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

    void calcRotScale(float anchorX,float anchorY,float oldX,float oldY,float newX,float newY,InputResult ir) {
        // convert input from display to object space using old display position and inputResult
        float[] dispV = {oldX,oldY,0.0f,1.0f};
        float[] dispNewV = {newX,newY,0.0f,1.0f};
        float[] anchorDispV = {anchorX,anchorY,0.0f,0.0f};
        float[] anchorDispVNew = new float[4];

        float[] disp2obj = new float[16];
        float[] obj2disp = new float[16];

        float[] objV = new float[4];
        float[] anchorObjV = new float[4];

        float is = 1.0f/ir.s;

        Matrix.setIdentityM(disp2obj, 0);
        Matrix.scaleM(disp2obj, 0, is, is, 1.0f);
        Matrix.rotateM(disp2obj, 0, -ir.r * (180.0f / (float) Math.PI), 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMV(objV, 0, disp2obj, 0, dispV, 0);
        //Log.e(TAG, "obj coords " + Arrays.toString(objV));
        Matrix.multiplyMV(anchorObjV, 0, disp2obj, 0, anchorDispV, 0);

        // figure out what rotation and scale to use to convert from object space back to new display position and inputResult
        // rotation
        float angObj = (float) Math.atan2(objV[1],objV[0]);
        //Log.e(TAG,"obj rotate " + angObj);
        float angDisp = (float) Math.atan2(newY,newX);
        //Log.e(TAG,"disp rotate " + angDisp);
        ir.r = angDisp - angObj;

        // scale
        float scaleObj = (float) Math.sqrt(objV[0]*objV[0] + objV[1]*objV[1]);
        float scaleDisp = (float) Math.sqrt(newX*newX + newY*newY);
        ir.s = scaleDisp/scaleObj;

        Matrix.setIdentityM(obj2disp, 0);
        Matrix.rotateM(obj2disp, 0, ir.r * (180.0f / (float) Math.PI), 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(obj2disp, 0, ir.s, ir.s, 1.0f);

        Matrix.multiplyMV(anchorDispVNew, 0, obj2disp, 0, anchorObjV, 0);
        ir.x += anchorDispVNew[0] - anchorDispV[0];
        ir.y += anchorDispVNew[1] - anchorDispV[1];
    }

    // one finger scale and rotate with an anchor at 0,0
    void calculate2() {
        if (oneInput != null) {
            inputResult.x += oneInput.currentX - oneInput.lastX;
            inputResult.y += oneInput.currentY - oneInput.lastY;
        } else if (twoFingers) {
            InputResult nir = new InputResult();

            // anchor Last 0 move 1
            float deltaCurrentX = inputState[1].currentX - inputState[0].lastX;
            float deltaCurrentY = inputState[1].currentY - inputState[0].lastY;
            float deltaLastX = inputState[1].lastX - inputState[0].lastX;
            float deltaLastY = inputState[1].lastY - inputState[0].lastY;
            float anchorX = inputResult.x - inputState[0].lastX;
            float anchorY = inputResult.y - inputState[0].lastY;
            calcRotScale(anchorX,anchorY,deltaLastX,deltaLastY,deltaCurrentX,deltaCurrentY,inputResult);

            // anchor move 0 anchor current 1
            deltaCurrentX = inputState[0].currentX - inputState[1].currentX;
            deltaCurrentY = inputState[0].currentY - inputState[1].currentY;
            deltaLastX = inputState[0].lastX - inputState[1].currentX;
            deltaLastY = inputState[0].lastY - inputState[1].currentY;
            anchorX = inputResult.x - inputState[1].currentX;
            anchorY = inputResult.y - inputState[1].currentY;
            calcRotScale(anchorX,anchorY,deltaLastX,deltaLastY,deltaCurrentX,deltaCurrentY,inputResult);
        }
    }

    public synchronized InputResult getResult() {
        InputResult ret = new InputResult(inputResult); // clone
        return ret;
    }

}
