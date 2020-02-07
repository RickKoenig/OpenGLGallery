package com.mperpetuo.openglgallery;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.mperpetuo.openglgallery.enga.StateMan;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.glut.MyGLRenderer;
import com.mperpetuo.openglgallery.input.OneFingerInput;
import com.mperpetuo.openglgallery.input.SimpleUI;
import com.mperpetuo.openglgallery.old_input.TwoFingerCollector;

public class MyGLSurfaceView extends GLSurfaceView {
    private final String TAG = "MyGLSurfaceView";

    // child and parent classes
    private MyGLRenderer mRenderer;

    public TwoFingerCollector tfc; // can't be final because if a constructor calls a function that sets this, it will fail.
    public OneFingerInput ofi; // for the new code, a start...
    public boolean doOld = false;
    public boolean doNew = !doOld;

    private void setupSurfaceView() {
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(this);
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data, fired off by requestRender
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        if (doOld)
            tfc = new TwoFingerCollector(); // use the TwoFingerCollector for old code
        if (doNew)
            ofi = new OneFingerInput(); // simple one finger input for now for the new code
    }

    // code friendly
    public MyGLSurfaceView(Context context) {
        super(context);
        if (isInEditMode())
            return;
        Log.i(TAG, "in MyGLSurfaceView constructor default");
        setupSurfaceView();
     }

    // layout friendly
    public MyGLSurfaceView(Context context,AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode())
            return;
        Log.i(TAG, "in MyGLSurfaceView constructor attrs");
        setupSurfaceView();
     }

    @Override
    protected void onSizeChanged(int a, int b, int c, int d) {
        Log.i(TAG, "size changed, new: " + a + " " + b + " old: " + c + " " + d);
    }

    @Override
    synchronized public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        if (doOld) {
            tfc.readInput(this, e);
            return true; // consume event
        }
        if (doNew) {
            ofi.readInput(e);
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        StateMan.changeState("");
        StateMan.proc();
        SimpleUI.reset();
    }

}
