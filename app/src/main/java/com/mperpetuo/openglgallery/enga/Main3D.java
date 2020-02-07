package com.mperpetuo.openglgallery.enga;

//import android.widget.Toast;

import android.widget.TextView;

import com.mperpetuo.openglgallery.MainActivity;
import com.mperpetuo.openglgallery.RunAvgLong;
import com.mperpetuo.openglgallery.input.SimpleUI;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_EXTENSIONS;

/**
 * Created by rickkoenig on 2/23/16.
 */

// this code runs on the opengl thread

public class Main3D {
    static final String TAG = "Main3D";
    static public int viewWidth,viewHeight; // important size of the view
    public static float viewAsp;
    static RunAvgLong mAvg = new RunAvgLong(100);
    static long lasttm = System.nanoTime();
    static float fpswanted = 60.0f;
    public static float frametime = 1.0f/fpswanted;
    public static String extensions;
    public static String[] extensionsSplit;
    public static boolean hasFloatTextures;
    public static boolean hasFloatLinearTextures;
    public static int maxTextures = 8;
    static int frameRateDelay;
    static boolean viewChanged;
    static boolean viewInited;

    static public void init(GL10 gl10) {
        //GLUtil.initShaders();
        //GLUtil.freeShaders();
        viewInited = false;
        extensions = gl10.glGetString(GL_EXTENSIONS);
        extensionsSplit = extensions.split(" ");
        for (String s : extensionsSplit) {
            if (s.equals("GL_OES_texture_float"))
                hasFloatTextures = true;
            if (s.equals("GL_OES_texture_float_linear"))
                hasFloatLinearTextures = true;
        }
        GLUtil.init();
        StateMan.init();
        Model.initModels();
        Texture.init();
        Lights.initlights();
        //ViewPort.mainvp = new ViewPort();
        //StateMan.changeState(StateList.startState);
        Utils.resetDirStack();
        SimpleUI.reset();
    }

    static public void changed(int width,int height) {
        viewWidth = width;
        viewHeight = height;
        viewAsp = (float)width/height;
        if (!viewInited) {
            ViewPort.mainvp = new ViewPort();
            viewInited = true;
        }
        //ViewPort.mainvp.virtX = .5f;
        //ViewPort.mainvp.virtY = .5f;

        //ViewPort.mainvp = new ViewPort();
        viewChanged = true;
    }

    static public void proc() throws Exception {
        if (viewChanged)
            StateMan.onResize(viewWidth,viewHeight);
        StateMan.proc();
        // calculate frame rate
        long tm = System.nanoTime();
        long dtm = tm - lasttm;
        lasttm = tm;
        dtm = mAvg.runavg(dtm);
        --frameRateDelay;
        viewChanged = false;
        if (frameRateDelay >= 0)
            return;
        frameRateDelay = 60;
        final String statusString = "fps = " + String.format("%7.3f",1e9f/dtm);
        final MainActivity act = Utils.getContext();
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = act.FPSmeterTextView;
                t.setText(statusString);
            }
        });
    }

}
