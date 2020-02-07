package com.mperpetuo.openglgallery.engatest;

import android.opengl.Matrix;
import android.util.Log;

import com.mperpetuo.openglgallery.enga.FrameBufferTexture;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.ViewPort;

import static com.mperpetuo.openglgallery.enga.ModelUtil.buildplanexyNt;

// make it easy to to interlace 3D
public class InterlaceAPI {

    final String TAG = "InterlaceAPI";

    int numTargets = 4;
    float gain;
    float convergence;
    boolean is3D;
    FrameBufferTexture[] frametexn = new FrameBufferTexture[4];
    ViewPort interleaveVP;
    Tree roottree;
    String mixShader;
    Tree fbnPlaneXY;


    public InterlaceAPI() {
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.getContext().show3DUI();
            }
        });

        for (int i=0;i<numTargets;++i) {
            frametexn[i] = FrameBufferTexture.createTexture("rendertex" + i, Main3D.viewWidth, Main3D.viewHeight);
        }

        interleaveVP = new ViewPort();

        // build the main screen scene last so we can hook up render targets, depends on FB 1
        roottree = new Tree("Interleave3Droot");
        mixShader = "interleave4";
        Log.i(TAG,"MIXSHADER = '" + mixShader);

        String[] textureList = new String[numTargets];
        for (int i = 0; i < numTargets; ++i) {
            String rt = "rendertex" + i;
            textureList[i] = rt;
        }
        //Interleave3D.fbnPlaneXY = buildplanexy("aplane",1,1,"Bark.png",mixShader);
        //Interleave3D.fbnPlaneXY = buildplanexy("aplane",1,1,"xpar.png",mixShader);
        fbnPlaneXY = buildplanexyNt("aplane",1,1,textureList,mixShader);
        fbnPlaneXY.scale = new float[] {1,-1,1};
        fbnPlaneXY.mod.flags &= ~Model.FLAG_HASALPHA;
        //fbnPlaneXY.mod.flags |= Model.FLAG_HASALPHA;
        fbnPlaneXY.mod.flags |= Model.FLAG_DOUBLESIDED; // draw backface since scale has a -1
        fbnPlaneXY.trans = new float[] {0,0,1};
        roottree.linkchild(fbnPlaneXY);
        onResize();
    }

    public void beginsceneAndDraw(ViewPort viewPort, Tree scene) {
        gain = Utils.getContext().getGain();
        convergence = Utils.getContext().getConv();
        is3D = Utils.getContext().getIs3D();
        //beginscene(viewPort);
        //scene.draw(); // depends on FB 1,2,3
        if (!is3D) {
            viewPort.beginscene();
            scene.draw();
            return;
        }
        float[] oldtrans = viewPort.trans.clone();

        // gain
        float[] transVec = new float[] {gain*2.0f/numTargets,0,0};

        // get viewport orientation matrix and move transVec to world space
        float[] tm = new float[16]; // matrix to spread cameras apart
        Matrix.setIdentityM(tm,0);
        Tree.buildtransrotscale(tm,viewPort.trans, viewPort.rot, null);
        NuMath.transformMat4Vec(transVec,transVec,tm);

        // now run through each render target and use appropriate viewport for each one for draw pass
        float nt = numTargets; // number of targets
        float gc = convergence*gain*2.0f/numTargets;
        for (int i = 0; i < nt; ++i) {
            FrameBufferTexture rt = frametexn[i]; // render target
            // place cameras in camera space and convert to world space
            float scl = i - nt*.5f + .5f; // 0 : -.5,.5 : -1,0,1 : -1.5,-.5,.5,1.5 etc...
            float conv = (i - nt*.5f +.5f)*gc;
            float[] trans = viewPort.trans;
            viewPort.xo = conv;
            NuMath.scale(trans,transVec,scl);
            NuMath.add(trans,trans,oldtrans);
            // draw FBn scene
            viewPort.target = rt;
            viewPort.beginscene();
            scene.draw();
        }
        viewPort.trans = oldtrans;
        viewPort.xo = 0;
        viewPort.target = null;

        interleaveVP.beginscene();
        if (Main3D.viewAsp >= 1.0f) {
            fbnPlaneXY.scale = new float[]{Main3D.viewAsp, -1, 1};
        } else {
            fbnPlaneXY.scale = new float[]{1, -1/Main3D.viewAsp, 1};
        }
        roottree.draw();
    }

    public void onResize() {
        int w = Main3D.viewWidth;
        int h = Main3D.viewHeight;
        Log.e(TAG,"the state 'Framebuffer4' resize event " + w + " " + h);
        //exitRenderTargets();
        //initRenderTargets();
        for (int i=0;i<numTargets;++i) {
            FrameBufferTexture rt = frametexn[i];
            rt.resize(w,h);
        }
        fbnPlaneXY.mat.put("resolution",new float[] {Main3D.viewWidth,Main3D.viewHeight});
    }

    public void glFree() {
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.getContext().hide3DUI();
            }
        });
        for (int i=0;i<numTargets;++i) {
            FrameBufferTexture fbt = frametexn[i];
            if (fbt == null)
                Log.e(TAG,"freeing a null FrameBufferTexture !!!");
            else
                fbt.glFree();
        }
        roottree.glFree();
        frametexn = null;
    }

}
