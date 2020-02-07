package com.mperpetuo.openglgallery.glut;

import static android.opengl.GLES20.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
//import android.widget.Toast;


import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.mperpetuo.openglgallery.GenericsTest;
import com.mperpetuo.openglgallery.SortTest;
import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.input.SimpleUI;
import com.mperpetuo.openglgallery.old_input.InputResult;
import com.mperpetuo.openglgallery.MainActivity;
import com.mperpetuo.openglgallery.MyGLSurfaceView;
import com.mperpetuo.openglgallery.RunAvgLong;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "MyGLRenderer";

    final boolean doOld; // proc the old code
    final boolean doNew; // proc the new code
    final boolean doThumbs = false; // load thumbnails

    // activity
    MainActivity act;
    // 3d objects
    //private Square mSquare;
    private Mesh_old mMesh;

    // matrices
    private final float[] mPositionMatrix = new float[16];

    // viewport
    public float destWidth,destHeight;
    public float destAspect; // of the display    width / height

    // texture
    public float srcAspect; // of the texture
    public float aspScaleX,aspScaleY; // for source aspect, scale the matrix

    int framecounter;

    int mProgram; // a shader


    // fps
    long lasttm = 0L;
    RunAvgLong mAvg;

    // parent class
    public static MyGLSurfaceView mView;

    // thumbnail textures
    ArrayList<Texture_old> textures = new ArrayList<>();
    int curTexture = 0;
    int texTime = 0;
    int texDuration = 60; //1/60 sec


    public MyGLRenderer(MyGLSurfaceView sf) {
    	super();
        Log.i(TAG, "MyGLRenderer constructor");
    	mView = sf;
        doNew = mView.doNew;
        doOld = mView.doOld;
        act = (MainActivity)mView.getContext();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig config) {
        /*
        SortTest st = new SortTest();
        st.test();
        */
        Log.i(TAG, "onSurfaceCreated");
        if (doNew)
            Main3D.init(gl10);
        if (!doOld)
            return;
        // Set the background frame color
        glClearColor(0.0f, 0.0f, .5f, 1.0f);
        // face culling
        glFrontFace(GL_CCW);
        glCullFace(GL_BACK);
        //glEnable(GL_CULL_FACE);
        // How big can points be for point cloud?
        FloatBuffer sizes = FloatBuffer.allocate(2);
        glGetFloatv(GL_ALIASED_POINT_SIZE_RANGE, sizes);
        Log.i(TAG, "pointcloud pointsizes min " + sizes.get(0) + " max " + sizes.get(1));

        ArrayList<File> thums = new ArrayList<>();
        if (doThumbs) {
            File dir = new File(act.CAPTURE_DIRECTORY);
            File[] list = dir.listFiles();
            if (list != null) {
                for (File d : list) {
                    //Log.e(TAG,"directory " + d);
                    File[] sublist = d.listFiles();
                    if (sublist != null) {
                        for (File sf : sublist) {
                            //Log.e(TAG,"sub directory " + sf);
                            String sfn = sf.toString();
                            if (sfn.contains("thumb_") && sfn.contains(".jpg")) {
                                Log.d(TAG, "thumb name " + sfn);
                                thums.add(sf);
                            }
                        }
                    }
                }
            }
            Log.i(TAG, "got thumbnail list!");
        }
        /*
        GenericsTest g = new GenericsTest();
        g.test();
        */


        //final BitmapFactory.Options options = new BitmapFactory.Options();
        int[] arr = new int[1];
        for (File thumb: thums) {
            Bitmap bm = BitmapFactory.decodeFile(thumb.getAbsolutePath());
            if (bm != null)
                Log.i(TAG, "thumb " + thumb + " has size " + bm.getWidth() + " " + bm.getHeight() + " texture id " + arr[0]);
            else {
                Log.i(TAG, "thumb " + thumb + " is NULL!, skipping");
                continue;
            }
            textures.add(new Texture_old(bm));
        }
        if (thums.size() == 0) {
            //Bitmap bm = BitmapFactory.decodeResource(mView.getResources(), R.drawable.caution);
            Bitmap bm = Utils.getBitmapFromAsset("common/caution.png");
            textures.add(new Texture_old(bm));
        }
        GLUtil.checkGlError("load thumbnails done");

        mProgram = GLUtil.loadShader( "tex_old");

        // init fps
        mAvg = new RunAvgLong(100);
        lasttm = System.nanoTime();

        // MESH
        final float squareCoords[] = {
                -1,-1,0,  0,1,   // bottom left
                1,-1,0,  1,1,   // bottom right
                -1, 1,0,  0,0,   // top left
                1, 1,0,  1,0 }; // top right
        final short faces[] = { 0, 1, 2, 3, 2, 1 }; // order to draw vertices
        int tex = GLUtil.loadShader( "tex_old");
        mMesh = new Mesh_old(squareCoords,faces,tex);

        // set the aspect ratios
        setAspScale(srcAspect);
    }

    void setAspScale(float srcAspect) {
        //Log.e(TAG, "Source aspect = " + srcAspect);
        boolean showall = true;
        if (showall) {
            //if (srcAspect < destAspect) {
            aspScaleX = srcAspect;
            aspScaleY = 1.0f;
            //} else {
            //    aspScaleX = 1.0f;
            //    aspScaleY = 1.0f/srcAspect;
            //}
        } else { // crop
            if (srcAspect < destAspect) {
                aspScaleX = destAspect;
                aspScaleY = destAspect/srcAspect;
            } else {
                aspScaleX = srcAspect/destAspect;
                aspScaleY = 1.0f/destAspect;
            }
        }
        //Log.e(TAG,"aspscaleX = " + aspScaleX + " aspscaleY " + aspScaleY);

    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        if (doNew)
            Main3D.changed(width,height);
        if (!doOld)
            return;
        Log.i(TAG, "onSurfacechanged to " + width + " " + height);
        // Adjust the viewport based on geometry changes, such as screen orientation
        destWidth = width;
        destHeight = height;
        // setup for destAspect
        destAspect = destWidth / destHeight;
        Log.i(TAG, "Dest aspect = " + destAspect);

        // make some 3d objects
        // SQUARE
        /*mSquare = new Square(mView.getContext());
        srcAspect = mSquare.getAspect();*/
    }

    // drawpass 2 is the only pass
    void drawPass(int pass) {
        switch (pass) {
            case 2: // draw to display
                // clear
                glViewport(0, 0, (int) destWidth, (int) destHeight);
                glEnable(GL_DEPTH_TEST);
                glClearColor(.5f, 1.0f, 1.0f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                Texture_old curtex = textures.get(curTexture);
                PointF curtexsize = curtex.getSize();
                setAspScale(curtexsize.x / curtexsize.y);

                // update position from touch input
                InputResult ir = mView.tfc.getResult();
                // It's all backwards !!
                Matrix.setIdentityM(mPositionMatrix, 0);
                if (destAspect > 1.0f)
                    Matrix.scaleM(mPositionMatrix,0,1.0f/ destAspect,1.0f,1.0f);
                else
                    Matrix.scaleM(mPositionMatrix,0,1.0f, destAspect,1.0f);
                Matrix.translateM(mPositionMatrix,0,ir.x,ir.y,0.0f);
                Matrix.rotateM(mPositionMatrix, 0, ir.r*180.0f/(float)Math.PI, 0.0f, 0.0f, 1.0f);
                Matrix.scaleM(mPositionMatrix, 0, ir.s, ir.s, ir.s);

                Matrix.scaleM(mPositionMatrix, 0, aspScaleX,aspScaleY,1.0f);

                /*
                // draw SQUARE
                if (mSquare != null) {
                    mSquare.draw(mPositionMatrix,mProgram,curtex.getTextureID());
                }
                 */
                // draw MESH
                mMesh.draw(mPositionMatrix,curtex.getTextureID());
                break;
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        MainActivity act = (MainActivity)mView.getContext();
        if (doNew) {
            // turn an Exception into a RuntimeException
            try {
                Main3D.proc();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("onDrawFrame Fail");
            }
        }
        if (!doOld)
            return;
        // OpenGL drawing
        //drawPass(1); // draw to framebuffer
        drawPass(2); // draw to display

        // calculate frame rate
        long tm = System.nanoTime();
        long dtm = tm - lasttm;
        lasttm = tm;
        dtm = mAvg.runavg(dtm);
        final String statusString = "fps = " + String.format("%7.3f",1e9f/dtm);
        //Globals.handler.post(Globals.runnable);
        /*act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast t = ((MainActivity)mView.getContext()).atoast;
                ((MainActivity)mView.getContext()).atoast.setText(statusString);
                t.show();
            }
        });*/

		// update frame counter
		++framecounter;

        // update texture counters
        ++texTime;
        if (texTime == texDuration) {
            texTime = 0;
            ++curTexture;
            if (curTexture == textures.size())
                curTexture = 0;
        }
    }

}

