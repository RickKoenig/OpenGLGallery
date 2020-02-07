package com.mperpetuo.openglgallery.enga;

import android.opengl.GLU;

/**
 * Created by cyberrickers on 8/8/2016.
 */
public class ShadowMap {
    static float[] LvMatrix = new float[16];
    static boolean inshadowmapbuild;

    static final float[] lightbias = {
            .5f,0,0,0,
            0,.5f,0,0,
            0,0,.5f,0,
            .5f,.5f,.5f,1.0f};


    // assemble a shadowmap render pass 1
    static void beginpass() {
        // copy this over for 2nd render pass
        System.arraycopy(GLUtil.pMatrix,0, GLUtil.LpMatrix,0,16);
        System.arraycopy(GLUtil.mvMatrix,0,LvMatrix,0,16);
        //globalmat.LpMatrix = mat4.clone(pMatrix); // copy this over for 2nd render pass
        //shadowmap.LvMatrix = mat4.clone(mvMatrix); // copy this over for 2nd render pass
        inshadowmapbuild = true; // models know to use shadowmapbuild shader
    }

    // disassemble a shadowmap render pass back to normal render target and prepare all matrices for shadowmapuse shader render pass 2
    static void endpass() {
        if (inshadowmapbuild) {
            NuMath.mul(GLUtil.LpMatrix,GLUtil.LpMatrix,LvMatrix);
            NuMath.mul(GLUtil.LpMatrix,GLUtil.LpMatrix,GLUtil.v2wMatrix);
            NuMath.mul(GLUtil.LpMatrix,lightbias,GLUtil.LpMatrix);
            //mat4.mul(globalmat.LpMatrix, globalmat.LpMatrix, shadowmap.LvMatrix);
            //mat4.mul(globalmat.LpMatrix, globalmat.LpMatrix, v2wMatrix);
            //mat4.mul(globalmat.LpMatrix, shadowmap.lightbias, globalmat.LpMatrix);
            inshadowmapbuild = false;
        }
    }

}
